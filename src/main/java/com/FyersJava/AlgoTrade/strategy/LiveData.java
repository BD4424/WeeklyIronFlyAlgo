package com.FyersJava.AlgoTrade.strategy;

import com.FyersJava.AlgoTrade.utility.Utility;
import com.tts.in.websocket.FyersSocket;
import com.tts.in.websocket.FyersSocketDelegate;
import in.tts.hsjavalib.ChannelModes;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LiveData implements FyersSocketDelegate {

    private final String symbol = "NSE:NIFTY50-INDEX";
    private FyersSocket fyersSocket;

    public void start() {
        fyersSocket = new FyersSocket(3);               // ✅ Initialize once
        fyersSocket.webSocketDelegate = this;
        fyersSocket.ConnectHSM(ChannelModes.FULL);      // ✅ Connect once
    }

    @Override
    public void OnOpen(String s) {
        System.out.println("WebSocket connection opened: " + s);

        // ✅ Subscribe using the same connected socket
        List<String> scripList = new ArrayList<>();
        scripList.add(symbol);

        System.out.println("Subscribing to: " + scripList);
        fyersSocket.SubscribeData(scripList);
    }

    @Override
    public void OnIndex(JSONObject jsonObject) {
        try {
            double ltp = jsonObject.optDouble("ltp");
            long timestamp = jsonObject.optLong("exch_feed_time");
            long prevClosePrice = jsonObject.optLong("prev_close_price");

            System.out.println("Symbol: " + symbol + ", ltp: " + ltp + ", timestamp: " +
                    Utility.readableTime(timestamp) + ", prevClosePrice: " + prevClosePrice);

            if (!Double.isNaN(ltp)) {
                // Handle your logic here
            }
        } catch (Exception ex) {
            System.err.println("Error in OnIndex: " + ex.getMessage());
        }
    }

    @Override
    public void OnScrips(JSONObject scrips) {
        try {
            String scripSymbol = scrips.optString("symbol");
            double ltp = scrips.optDouble("ltp");
            long timestamp = scrips.optLong("last_traded_time");

            System.out.println("Tick: " + scripSymbol + " => " + ltp + " @ " + Utility.readableTime(timestamp));

            if (!Double.isNaN(ltp)) {
                WeeklyIronFly strategy = new WeeklyIronFly();
                strategy.onCandle(scripSymbol, ltp);
            }
        } catch (Exception ex) {
            System.err.println("Error in OnScrips: " + ex.getMessage());
        }
    }

    @Override public void OnDepth(JSONObject jsonObject) {}
    @Override public void OnOrder(JSONObject jsonObject) {}
    @Override public void OnTrade(JSONObject jsonObject) {}
    @Override public void OnPosition(JSONObject jsonObject) {}

    @Override
    public void OnError(JSONObject jsonObject) {
        System.err.println("Socket Error: " + jsonObject.toString(2));
    }

    @Override
    public void OnClose(String s) {
        System.out.println("Socket closed: " + s);
    }

    @Override
    public void OnMessage(JSONObject jsonObject) {
        System.out.println("Raw message: " + jsonObject.toString(2));
    }
}
