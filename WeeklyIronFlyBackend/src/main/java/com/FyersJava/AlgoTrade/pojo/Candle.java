package com.FyersJava.AlgoTrade.pojo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Candle {
    public long timestamp;
    public double open;
    public double high;
    public double low;
    public double close;


    public Candle(long timestamp, double open, double high, double low, double close) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public String getFormattedTime() {
        Date date = new Date(timestamp*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Change timezone if needed
        return sdf.format(date);
    }

    @Override
    public String toString() {
        return "Candle{" +
                "timestamp=" + timestamp +
                ", time='" + getFormattedTime() + '\'' +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                '}';
    }
}
