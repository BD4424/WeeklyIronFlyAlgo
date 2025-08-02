package com.FyersJava.AlgoTrade.utility;

import com.FyersJava.AlgoTrade.pojo.Candle;
import com.tts.in.model.FyersClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class Utility {

    private static final Set<LocalDate> nseHolidays = new HashSet<>(Arrays.asList(
            LocalDate.of(2025, 1, 26),  // Republic Day
            LocalDate.of(2025, 3, 31),
            LocalDate.of(2025, 4, 10), //Shri Mahavir Jayanti
            LocalDate.of(2025, 10, 02),
            LocalDate.of(2025, 12, 25),// Holi
            LocalDate.of(2025, 8, 15)   // Independence Day
    ));

    public static String readableTime(long timestamp){
        Date date = new Date(timestamp*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Change timezone if needed
        return sdf.format(date);
    }

    public static double extractSpotFromQuotes(JSONObject stockJson) {
        try {
            JSONArray data = stockJson.getJSONArray("d");
            JSONObject v = data.getJSONObject(0).getJSONObject("v");

            double lp = v.getDouble("lp");     // Spot price (last traded)
            long tt = v.getLong("tt");         // Timestamp if needed

            System.out.println("Spot (lp): " + lp);
            System.out.println("Timestamp (tt): " + tt);

            return lp;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid stock quote JSON structure.");
        }
    }

    /**
     * Finds the strike price for a given delta.
     */


    public static String weeklyOptionSymbolGeneratorNifty(String strikePrice){
        LocalDate today = LocalDate.now();
        LocalDate expiry = getValidExpiryThursday(today);

        // Dynamic Thursday expiry
        LocalDate expiryThursday = (today.getDayOfWeek().getValue() <= DayOfWeek.THURSDAY.getValue())
                ? today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY))
                : today.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));

        String yy = String.valueOf(expiry.getYear()).substring(2); // "25"
        String Mdd = expiry.getMonthValue() + String.format("%02d", expiry.getDayOfMonth()); // "724"

        String strikeSymbol1 = createFullStrike(strikePrice,"CE");
        String strikeSymbol2 = createFullStrike(strikePrice,"PE");

        return strikeSymbol1+"::"+strikeSymbol2;
    }

    public static String createFullStrike(String strikePrice, String strikeType){
        LocalDate today = LocalDate.now();
        LocalDate expiry = getValidExpiryThursday(today);
        String yy = String.valueOf(expiry.getYear()).substring(2);
        String Mdd = expiry.getMonthValue() + String.format("%02d", expiry.getDayOfMonth());

        return "NSE:NIFTY" + yy + Mdd + strikePrice + strikeType;
    }

    public static LocalDate getValidExpiryThursday(LocalDate today) {
        LocalDate thursday = (today.getDayOfWeek().getValue() <= DayOfWeek.THURSDAY.getValue())
                ? today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY))
                : today.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));

        // Check if Thursday is a holiday
        while (nseHolidays.contains(thursday)) {
            // Move to previous day until it's not a holiday (usually Wed or earlier)
            thursday = thursday.minusDays(1);
        }
        return thursday;
    }

    public static String getLastTradingDate() {
        LocalDate date = LocalDate.now().minusDays(1); // Start with yesterday

        while (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY ||
                nseHolidays.contains(date)) {
            date = date.minusDays(1); // Keep going back
        }

        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Return in yyyy-MM-dd format
    }

    public static double findConsolidationHigh(List<Candle> candles, double thresholdPercentage) {
        double highestHigh = 0;
        for (int i = 0; i < candles.size() - 1; i++) {
            double range = Math.abs(candles.get(i).high - candles.get(i).low);
            double rangePercent = (range / candles.get(i).low) * 100;

            if (rangePercent < thresholdPercentage) {  // e.g., less than 0.5%
                highestHigh = Math.max(highestHigh, candles.get(i).high);
            }
        }
        return highestHigh;
    }

    public static List<Double> getTodayLowHighPrice(FyersClass fyersClass, String symbol) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = LocalDate.now().format(formatter); // get "2025-07-18" or earlier valid date

        List<Candle> candles = HistoricalData.stockHistoryByStock(
                fyersClass,
                symbol,
                "1",      // dateFormat = 1 means yyyy-MM-dd
                "D",      // Daily candle
                date,
                date      // same for fromDate and toDate
        );

        if (!candles.isEmpty()) {
            return Arrays.asList(candles.get(0).low,candles.get(0).high);
        } else {
            System.out.println("No candle found for valid date: " + date);
            return null;
        }
    }
    public static List<Double> getYesterdaysCloseHighPrice(FyersClass fyersClass, String symbol) {
        String date = getLastTradingDate(); // get "2025-07-18" or earlier valid date

        List<Candle> candles = HistoricalData.stockHistoryByStock(
                fyersClass,
                symbol,
                "1",      // dateFormat = 1 means yyyy-MM-dd
                "D",      // Daily candle
                date,
                date      // same for fromDate and toDate
        );

        if (!candles.isEmpty()) {
            return Arrays.asList(candles.get(0).close,candles.get(0).high);
        } else {
            System.out.println("No candle found for valid date: " + date);
            return null;
        }
    }

    public static void main(String[] args) {
        weeklyOptionSymbolGeneratorNifty("25100");
    }
}
