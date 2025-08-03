package com.FyersJava.AlgoTrade.pojo;

public class LiveCandle {
    public long startTime;
    public double open;
    public double high;
    public double low;
    public double close;
    public long volume;

    public LiveCandle(long startTime, double price, long volume) {
        this.startTime = startTime;
        this.open = this.high = this.low = this.close = price;
        this.volume = volume;
    }

    public void update(double price, long tradeQty) {
        this.close = price;
        this.high = Math.max(this.high, price);
        this.low = Math.min(this.low, price);
        this.volume += tradeQty;
    }
}
