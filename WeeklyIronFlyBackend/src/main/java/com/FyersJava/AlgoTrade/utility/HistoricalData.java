package com.FyersJava.AlgoTrade.utility;

import com.FyersJava.AlgoTrade.pojo.Candle;
import com.tts.in.model.FyersClass;
import com.tts.in.model.StockHistoryModel;
import com.tts.in.utilities.Tuple;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoricalData {
    public static void stockHistory(FyersClass fyersClass) {
        StockHistoryModel model = new StockHistoryModel();
        model.Symbol = "NSE:SBIN-EQ";
        model.Resolution = "30";
        model.DateFormat = "1";
        model.RangeFrom = "2025-01-01";
        model.RangeTo = "2025-01-15";
        model.ContFlag = 1;

        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);
        if (stockTuple.Item2() == null) {
            System.out.println("Stock History: " + stockTuple.Item1());
        } else {
            System.out.println("Stock History Error: " + stockTuple.Item2());
        }
    }

    public static List<Candle> stockHistoryByStock(FyersClass fyersClass, String symbol, String dateFormat, String candleResolution,
                                                   String fromDate, String toDate) {
        StockHistoryModel model = new StockHistoryModel();
        model.Symbol = symbol;
        model.Resolution = candleResolution;
        model.DateFormat = dateFormat;
        model.RangeFrom = fromDate;
        model.RangeTo = toDate;
        model.ContFlag = 1;

        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);
        List<Candle> historicalData = new ArrayList<>();
        if (stockTuple.Item2() == null) {
//            System.out.println("Stock History: " + stockTuple.Item1());
            JSONObject response = stockTuple.Item1();
            JSONArray candles = response.getJSONArray("candles");
            for (int i=0;i< candles.length();i++){
                JSONArray candle = candles.getJSONArray(i);
                Candle candle1 = new Candle(candle.getLong(0),candle.getDouble(1),
                        candle.getDouble(2),candle.getDouble(3),candle.getDouble(4));
                historicalData.add(candle1);
            }

        } else {
            System.out.println("Stock History Error: " + stockTuple.Item2());
        }

        return historicalData;
    }
}
