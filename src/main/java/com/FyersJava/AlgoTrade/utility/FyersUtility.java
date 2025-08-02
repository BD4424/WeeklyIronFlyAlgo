package com.FyersJava.AlgoTrade.utility;

import com.FyersJava.AlgoTrade.pojo.OptionChainEntry;
import com.tts.in.model.FyersClass;
import com.tts.in.utilities.Tuple;
import org.json.JSONObject;

public class FyersUtility {

    public static OptionChainEntry GetStockQuotes(FyersClass fyersClass, String symbol) {
        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockQuotes(symbol);

        if (stockTuple.Item2() == null) {
            System.out.println("Stock Quotes:" + stockTuple.Item1());
            return parseStockQuoteToOptionEntry(stockTuple.Item1());
        } else {
            System.err.println("Error: " + stockTuple.Item2());
            throw new RuntimeException("Could not fetch stock quotes!");
        }
    }

    public static Tuple<JSONObject, JSONObject> GetStockQuotesForIndex(FyersClass fyersClass, String symbol) {

        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockQuotes(symbol);

        if (stockTuple.Item2() == null) {
            System.out.println("Stock Quotes:" + stockTuple.Item1());
        } else {
            System.out.println("Error: " + stockTuple.Item2());
        }

        return stockTuple;

    }

    public static Boolean GetProfile(FyersClass fyersClass) {
        Tuple<JSONObject, JSONObject> ProfileResponseTuple = fyersClass.GetProfile();

        if (ProfileResponseTuple.Item2() == null) {
            System.out.println("Profile: " + ProfileResponseTuple.Item1());
            return true;
        } else {
            System.out.println("Profile Error: " + ProfileResponseTuple.Item2());
            return false;
        }
    }

    public static OptionChainEntry parseStockQuoteToOptionEntry(JSONObject stockQuoteJson) {
        JSONObject data = stockQuoteJson.getJSONArray("d").getJSONObject(0).getJSONObject("v");

        String symbol = data.getString("symbol"); // e.g., NSE:BANKNIFTY25JUL56600PE
        double ltp = data.getDouble("lp");

        // Extract strike price from symbol using regex or split
        // Example symbol: "NSE:BANKNIFTY25JUL56600PE"
        String[] parts = symbol.split("(?<=\\d)(?=PE|CE)"); // Split before "PE"/"CE"
        String pricePart = parts[0].replaceAll("[^0-9]", ""); // Get numeric part
        int strikePrice = Integer.parseInt(pricePart.substring(pricePart.length() - 5)); // Last 5 digits assumed to be strike

        String optionType = symbol.endsWith("CE") ? "CE" : "PE";

        return new OptionChainEntry(strikePrice, symbol, optionType, ltp);
    }

}
