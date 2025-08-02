package com.FyersJava.AlgoTrade.pojo;

public class OptionChainEntry {
    public int strikePrice;
    public String strikeSymbol;
    public String optionType;
    public double ltp;

    public OptionChainEntry(int strikePrice, String strikeSymbol, String optionType, double ltp) {
        this.strikePrice = strikePrice;
        this.strikeSymbol = strikeSymbol;
        this.optionType = optionType;
        this.ltp = ltp;
    }

    @Override
    public String toString() {
        return "StrikePrice: " + strikePrice + ", Symbol: " + strikeSymbol + ", Type: " + optionType + ", LTP: " + ltp;
    }
}
