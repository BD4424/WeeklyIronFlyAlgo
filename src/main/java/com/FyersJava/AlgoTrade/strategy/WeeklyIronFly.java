package com.FyersJava.AlgoTrade.strategy;

import com.FyersJava.AlgoTrade.controller.CombinedValueBroadcaster;
import com.FyersJava.AlgoTrade.entity.TradeGroup;
import com.FyersJava.AlgoTrade.entity.TradeLog;
import com.FyersJava.AlgoTrade.pojo.Candle;
import com.FyersJava.AlgoTrade.repo.TradeGroupRepo;
import com.FyersJava.AlgoTrade.repo.TradeLogRepo;
import com.FyersJava.AlgoTrade.utility.FyersUtility;
import com.FyersJava.AlgoTrade.utility.HistoricalData;
import com.FyersJava.AlgoTrade.utility.OptionGreeks;
import com.FyersJava.AlgoTrade.utility.Utility;
import com.tts.in.model.FyersClass;
import com.tts.in.utilities.Tuple;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WeeklyIronFly {

    @Autowired
    TradeGroupRepo tradeGroupRepo;
    private static final Logger log = LoggerFactory.getLogger(WeeklyIronFly.class);
    @Autowired
    TradeLogRepo tradeLogRepo;

    @Autowired
    private CombinedValueBroadcaster broadcaster;

    boolean isPosition = false;
    Double stopLoss;
    Double entryPremium;
    boolean reEntry = false;
    String symbolCE;
    String symbolPE;
    String symbolCeHedge;
    String symbolPeHedge;
    TradeGroup trade;
    boolean isCheckedDB = false;


    public void onCandle(String symbol, double ltp) {
        LocalTime currentTime = LocalTime.now();
        LocalDate today = LocalDate.now();

        if (today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY ||
                currentTime.isBefore(LocalTime.of(9, 15)) || currentTime.isAfter(LocalTime.of(15, 30))) {
            log.info("Market is closed");
            return;
        }
        if (trade == null && !isCheckedDB) {
            List<TradeGroup> activeTradeGroups = tradeGroupRepo.findActiveTradeGroups();
            if (!activeTradeGroups.isEmpty()) {
                trade = activeTradeGroups.get(0);
                isPosition = true;
                List<TradeLog> ceLegs = trade.getLegs().stream().filter(tradeLog -> tradeLog.getStrikePrice().endsWith("CE")).toList();
                List<TradeLog> peLegs = trade.getLegs().stream().filter(tradeLog -> tradeLog.getStrikePrice().endsWith("PE")).toList();
                int i = 0;
                for (TradeLog ceLeg : ceLegs) {
                    String strike = ceLeg.getStrikePrice().substring(ceLeg.getStrikePrice().length() - 7, ceLeg.getStrikePrice().length() - 2);
                    if (peLegs.get(i).getStrikePrice().substring(ceLeg.getStrikePrice().length() - 7, ceLeg.getStrikePrice().length() - 2).equals(strike)) {
                        symbolCE = ceLeg.getStrikePrice();
                        symbolPE = peLegs.get(i).getStrikePrice();
                    }
                    i++;
                }
                symbolCeHedge = ceLegs.stream().filter(tradeLog -> !tradeLog.getStrikePrice().equals(symbolCE)).toList().get(0).getStrikePrice();
                symbolPeHedge = peLegs.stream().filter(tradeLog -> !tradeLog.getStrikePrice().equals(symbolPE)).toList().get(0).getStrikePrice();
                stopLoss = trade.getStopLoss();
            }
            isCheckedDB = true;
        }

        // If today is Friday AND time is before 9:30 AM, return early
        if (today.getDayOfWeek() == DayOfWeek.FRIDAY && currentTime.isBefore(LocalTime.of(9, 30))) {
            log.info("Today is Friday. Strategy will execute after 9:30 AM.");
            return;
        }

        if (symbolCE != null && getDaysToExpiryBySymbol(symbolCE) == 0 && !isPosition && currentTime.isAfter(LocalTime.of(14, 55))) {
            log.info("Trade has been closed for this week");
            return;
        }

        if (!isPosition) {
            int atm = getNearestSP(ltp);

            String combinedSP = Utility.weeklyOptionSymbolGeneratorNifty(String.valueOf(atm));
            symbolCE = combinedSP.split("::")[0];
            symbolPE = combinedSP.split("::")[1];

            double combinedCurrentVal = FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolCE).ltp + FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolPE).ltp;
            broadcaster.broadcastCombinedValue(combinedCurrentVal);

            log.info("Current combined value: " + combinedCurrentVal);
            if (entryPremium == null) {
                if (today.getDayOfWeek() == DayOfWeek.FRIDAY && !reEntry) {
                    List<Double> combinedClosingCloseAndHighThursday = combinedClosingCloseAndHighThursday(String.valueOf(atm));
                    stopLoss = combinedClosingCloseAndHighThursday.get(1);
                    entryPremium = combinedClosingCloseAndHighThursday.get(0);
                } else {
                    //For re-entry
                    if (reEntry) {
                        entryPremium = Objects.requireNonNull(Utility.getTodayLowHighPrice(FyersClass.getInstance(), symbolCE)).get(0)
                                + Objects.requireNonNull(Utility.getTodayLowHighPrice(FyersClass.getInstance(), symbolPE)).get(0);
                    } else {
                        entryPremium = Objects.requireNonNull(Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolCE)).get(0)
                                + Objects.requireNonNull(Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolPE)).get(0);
                    }
                }
            }
            log.info("Entry premium: " + entryPremium);

            if (!(today.getDayOfWeek() == DayOfWeek.FRIDAY) && currentTime.isAfter(LocalTime.of(9, 00)) && reEntry) {
                List<Candle> ceCandles = HistoricalData.stockHistoryByStock(FyersClass.getInstance(), symbolCE, "1",
                        "30", String.valueOf(today.minusDays(7)), String.valueOf(LocalDate.now()));
                List<Candle> peCandles = HistoricalData.stockHistoryByStock(FyersClass.getInstance(), symbolPE, "1",
                        "30", String.valueOf(today.minusDays(7)), String.valueOf(LocalDate.now()));
                stopLoss = getCombinedSwingHigh(ceCandles, peCandles);
            } else if (!reEntry){
                //logical error. need to get swing high combined not idividual
                stopLoss = Objects.requireNonNull(Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolCE)).get(1)
                        + Objects.requireNonNull(Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolPE)).get(1);

            }

            if (combinedCurrentVal < entryPremium) {
                //Enter trade on Friday
                Tuple<JSONObject, JSONObject> stockTuple = FyersUtility.GetStockQuotesForIndex(FyersClass.getInstance(), symbol);
                double spot = Utility.extractSpotFromQuotes(stockTuple.Item1());
                double ceHedge = OptionGreeks.findStrikeForTargetDelta(spot, 0.2, getDaysToExpiryBySymbol(symbolCE) / 365.0, 0.06, 0.12, true);
                double peHedge = OptionGreeks.findStrikeForTargetDelta(spot, -0.2, getDaysToExpiryBySymbol(symbolPE) / 365.0, 0.06, 0.12, false);
                symbolCeHedge = Utility.createFullStrike(String.valueOf(getNearestSP(ceHedge)),"CE");
                symbolPeHedge = Utility.createFullStrike(String.valueOf(getNearestSP(peHedge)),"PE");

                ironFly(symbolCE, symbolPE, symbolPeHedge, symbolCeHedge, combinedCurrentVal);
            }
        } else {
            double combinedCurrentVal = FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolCE).ltp + FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolPE).ltp;
            //Monitor for SL
            log.info("Current combined value: "+combinedCurrentVal);
            if (combinedCurrentVal > stopLoss) {
                //exit trade
                isPosition = false;
                double pnl1 = entryToTradeLogDBAfterSL(symbolCE, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolCE).ltp).getNetPnl();
                double pnl2 = entryToTradeLogDBAfterSL(symbolPE, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolPE).ltp).getNetPnl();
                double pnl3 = entryToTradeLogDBAfterSL(symbolCeHedge, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolCeHedge).ltp).getNetPnl();
                double pnl4 = entryToTradeLogDBAfterSL(symbolPeHedge, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolPeHedge).ltp).getNetPnl();

                trade.setNetPnl(pnl1 + pnl2 + pnl3 + pnl4);
                trade.setIsActive(false);
                trade.setExitType("SL");
                trade.setExitTime(LocalDateTime.now());
                tradeGroupRepo.save(trade);
                reEntry = true;

            } else {
                //continue
            }

            //If today is expiry day. Then close trade and store details in DB
            if (symbolCE != null && getDaysToExpiryBySymbol(symbolCE) == 0 && isPosition && currentTime.isAfter(LocalTime.of(14, 55))) {
                //square off
                isPosition = false;
                double pnl1 = entryToTradeLogDBAtExpiryEnd(symbolCE, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolCE).ltp).getNetPnl();
                double pnl2 = entryToTradeLogDBAtExpiryEnd(symbolPE, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolPE).ltp).getNetPnl();
                double pnl3 = entryToTradeLogDBAtExpiryEnd(symbolCeHedge, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolCeHedge).ltp).getNetPnl();
                double pnl4 = entryToTradeLogDBAtExpiryEnd(symbolPeHedge, FyersUtility.GetStockQuotes(FyersClass.getInstance(), symbolPeHedge).ltp).getNetPnl();

                trade.setNetPnl(pnl1 + pnl2 + pnl3 + pnl4);
                trade.setIsActive(isPosition);
                trade.setExitType("EXPIRY_EXIT");
                trade.setExitTime(LocalDateTime.now());
                tradeGroupRepo.save(trade);

                //Write some logic to reset everything and start afresh from next day
                resetState();
            }

        }
    }

    public Integer getNearestSP(Double val){
        return  (int) (Math.round(val / 50.0) * 50);
    }


    private void resetState() {
        symbolCE = null;
        symbolPE = null;
        symbolCeHedge = null;
        symbolPeHedge = null;
        entryPremium = null;
        stopLoss = null;
        trade = null;
        isPosition = false;
    }


    public static int getDaysToExpiryBySymbol(String symbol) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        // Extract the part before CE/PE (find first "CE" or "PE")
        int ceIndex = symbol.indexOf("CE");
        int peIndex = symbol.indexOf("PE");

        int index = (ceIndex != -1) ? ceIndex : peIndex;
        if (index < 2) {
            throw new IllegalArgumentException("Invalid option symbol: missing CE/PE or insufficient length");
        }

        // Get two characters before CE/PE and after strike
        String expiryDayStr = symbol.substring(index - 7, index - 5); // 7 chars back = 5-digit strike, 2-digit expiry day
        int expiryDay = Integer.parseInt(expiryDayStr);

        int currentDay = today.getDayOfMonth();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        int expiryMonth = currentMonth;
        int expiryYear = currentYear;

        if (expiryDay < currentDay) {
            // Expiry is in the next month
            expiryMonth++;
            if (expiryMonth > 12) {
                expiryMonth = 1;
                expiryYear++;
            }
        }

        // Adjust expiry day if it's beyond last day of expiry month (e.g., 31 in Feb)
        YearMonth expiryYearMonth = YearMonth.of(expiryYear, expiryMonth);
        int lastDay = expiryYearMonth.lengthOfMonth();
        if (expiryDay > lastDay) {
            expiryDay = lastDay;
        }

        LocalDate expiryDate = LocalDate.of(expiryYear, expiryMonth, expiryDay);
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
    }

    public static List<Candle> mergeCandlesByTimestamp(List<Candle> ceCandles, List<Candle> peCandles) {
        Map<Long, Candle> peMap = peCandles.stream()
                .collect(Collectors.toMap(c -> c.timestamp, c -> c));

        List<Candle> mergedCandles = new ArrayList<>();

        for (Candle ce : ceCandles) {
            Candle pe = peMap.get(ce.timestamp);
            if (pe != null) {
                double combinedOpen = ce.open + pe.open;
                double combinedHigh = ce.high + pe.high;
                double combinedLow = ce.low + pe.low;
                double combinedClose = ce.close + pe.close;

                mergedCandles.add(new Candle(ce.timestamp, combinedOpen, combinedHigh, combinedLow, combinedClose));
            }
        }

        return mergedCandles;
    }

    public static LocalDate getPreviousTradingDate(List<Candle> candles, LocalDate today) {
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");

        // Collect all unique trading dates in ascending order
        TreeSet<LocalDate> tradingDates = candles.stream()
                .map(c -> Instant.ofEpochSecond(c.timestamp).atZone(zoneId).toLocalDate())
                .filter(d -> d.isBefore(today)) // only before today
                .collect(Collectors.toCollection(TreeSet::new));

        // Get the last date before today
        return tradingDates.isEmpty() ? null : tradingDates.last();
    }

    public static double getCombinedSwingHigh(List<Candle> ceCandles, List<Candle> peCandles) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate previousTradingDay = getPreviousTradingDate(ceCandles, today);

        if (previousTradingDay == null) {
            System.out.println("No previous trading data found.");
            return Double.NaN;
        }

        List<Candle> combinedCandles = mergeCandlesByTimestamp(ceCandles, peCandles);
        System.out.println("Combined candles: " + combinedCandles);
        double swingHigh = Utility.findConsolidationHigh(combinedCandles, autoThreshold(combinedCandles));

        System.out.println("Corrected SL from last combined swing high: " + swingHigh);
        return swingHigh;
    }

    public static double autoThreshold(List<Candle> candles) {
        double avgPrice = candles.stream()
                .mapToDouble(c -> (c.high + c.low) / 2)
                .average()
                .orElse(100); // fallback

        if (avgPrice > 1000) return 2; // BNF
        if (avgPrice > 400) return 3;
        return 4.0; // Nifty or low premium options
    }

    public static List<Double> combinedClosingCloseAndHighThursday(String strikeIndex) {
        String symbol = Utility.weeklyOptionSymbolGeneratorNifty(strikeIndex);
        String symbolCE = symbol.split("::")[0];
        String symbolPE = symbol.split("::")[1];

        Double yesterdaysClosePriceCE = Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolCE).get(0);
        Double yesterdaysClosePricePE = Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolPE).get(0);

        Double yesterdaysHighPriceCE = Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolCE).get(1);
        Double yesterdaysHighPricePE = Utility.getYesterdaysCloseHighPrice(FyersClass.getInstance(), symbolPE).get(1);

        return Arrays.asList(yesterdaysClosePriceCE + yesterdaysClosePricePE, yesterdaysHighPriceCE + yesterdaysHighPricePE);
    }

    public void ironFly(String atmStrikeCE, String atmStrikePE, String putHedge, String callHedge, Double ltp) {
        //Get prices of all premium.
        Double atmStrikeCEPrice = FyersUtility.GetStockQuotes(FyersClass.getInstance(), atmStrikeCE).ltp;
        Double atmStrikePEPrice = FyersUtility.GetStockQuotes(FyersClass.getInstance(), atmStrikePE).ltp;
        Double hedgeCallPrice = FyersUtility.GetStockQuotes(FyersClass.getInstance(), callHedge).ltp;
        Double hedgePutPrice = FyersUtility.GetStockQuotes(FyersClass.getInstance(), putHedge).ltp;

        // 1. Create and save TradeGroup first
        trade = new TradeGroup();
        trade.setEntryTime(LocalDateTime.now());
        trade.setIsActive(true);
        trade.setStopLoss(stopLoss);
        trade.setTradeEntryIndex(ltp);
        trade.setStrategy("IRON_FLY");

        // Save it first to get a managed entity (especially if ID is auto-generated)
        tradeGroupRepo.save(trade);

        // 2. Create TradeLog entries and link the group
        TradeLog tradeLogCESell = entryToTradeLogDB(atmStrikeCE, atmStrikeCEPrice, trade);
        TradeLog tradeLogPESell = entryToTradeLogDB(atmStrikePE, atmStrikePEPrice, trade);
        TradeLog tradeLogCEBuy = entryToTradeLogDB(callHedge, hedgeCallPrice, trade);
        TradeLog tradeLogPEBuy = entryToTradeLogDB(putHedge, hedgePutPrice, trade);

        // 3. Save all TradeLogs
        tradeLogRepo.saveAll(List.of(tradeLogCEBuy, tradeLogPEBuy, tradeLogCESell, tradeLogPESell));

        // 4. Set the list in group and update it if needed
        trade.setLegs(List.of(tradeLogCEBuy, tradeLogPEBuy, tradeLogCESell, tradeLogPESell));
        tradeGroupRepo.save(trade); // optional, just to update the legs
        isPosition = true;
    }

    public TradeLog entryToTradeLogDBAtExpiryEnd(String strikeSymbol, Double price) {
        TradeLog tradeLog = tradeLogRepo.findActiveByStrikePrice(strikeSymbol).get(0);
        tradeLog.setExitTime(LocalDateTime.now());
        tradeLog.setExitPremium(price);
        tradeLog.setIsActive(isPosition);
        tradeLog.setNetPnl(price - tradeLog.getEntryPremium());

        return tradeLogRepo.save(tradeLog);
    }

    public TradeLog entryToTradeLogDBAfterSL(String strikeSymbol, Double price) {
        TradeLog tradeLog = tradeLogRepo.findActiveByStrikePrice(strikeSymbol).get(0);
        tradeLog.setExitTime(LocalDateTime.now());
        tradeLog.setExitPremium(price);
        tradeLog.setIsActive(isPosition);
        tradeLog.setNetPnl(price - tradeLog.getEntryPremium());

        return tradeLogRepo.save(tradeLog);
    }

    private TradeLog entryToTradeLogDB(String symbol, Double premium, TradeGroup tradeGroup) {
        TradeLog tradeLog = new TradeLog();
        tradeLog.setStrikePrice(symbol);
        tradeLog.setEntryTime(LocalDateTime.now());
        tradeLog.setEntryPremium(premium);
        tradeLog.setGroup(tradeGroup);
        tradeLog.setIsActive(true);
        tradeLog.setStrategy("IRON_FLY");
        return tradeLog;
    }

}
