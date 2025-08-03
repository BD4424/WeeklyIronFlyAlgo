package com.FyersJava.AlgoTrade.utility;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

import static java.lang.Math.*;

public class OptionGreeks {

    public static double calculateCallDelta(double S, double K, double T, double r, double sigma) {
        double d1 = (log(S / K) + (r + sigma * sigma / 2) * T) / (sigma * sqrt(T));
        return cumulativeNormal(d1);
    }

    public static double calculatePutDelta(double S, double K, double T, double r, double sigma) {
        double d1 = (log(S / K) + (r + sigma * sigma / 2) * T) / (sigma * sqrt(T));
        return cumulativeNormal(d1) - 1;
    }

    // Cumulative normal distribution (approximation)
    private static double cumulativeNormal(double x) {
        return 0.5 * (1.0 + erf(x / sqrt(2.0)));
    }

    private static double erf(double z) {
        // Taylor expansion (simplified)
        double t = 1.0 / (1.0 + 0.5 * abs(z));
        double ans = 1 - t * exp(-z*z - 1.26551223 +
                 t * (1.00002368 +
                 t * (0.37409196 +
                 t * (0.09678418 +
                 t * (-0.18628806 +
                 t * (0.27886807 +
                 t * (-1.13520398 +
                 t * (1.48851587 +
                 t * (-0.82215223 +
                 t * 0.17087277)))))))));
        return z >= 0 ? ans : -ans;
    }

    public static double findStrikeForTargetDelta(double spot, double targetDelta, double T, double r, double atmIv, boolean isCall) {
        double closestStrike = spot;
        double closestDeltaDiff = Double.MAX_VALUE;

        // Start from spot - 1000 to spot + 1000 and step in 50s
        for (double strike = spot - 1000; strike <= spot + 1000; strike += 50) {
            // Approximate IV skew: add 0.01 if OTM
            double adjustedIv = atmIv;
            if (strike > spot && isCall) adjustedIv += 0.01;
            if (strike < spot && !isCall) adjustedIv += 0.01;

            double delta = isCall
                    ? calculateCallDelta(spot, strike, T, r, adjustedIv)
                    : calculatePutDelta(spot, strike, T, r, adjustedIv);

            double deltaDiff = abs(delta - targetDelta);

            if (deltaDiff < closestDeltaDiff) {
                closestDeltaDiff = deltaDiff;
                closestStrike = strike;
            }

            if (deltaDiff < 0.005) break; // stop early if match is very close
        }

        return closestStrike;
    }

    public static int getDaysToExpiryBySymbol(String symbol) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        // Extract the part before CE/PE (find first "CE" or "PE")
        int ceIndex = symbol.indexOf("CE");
        int peIndex = symbol.indexOf("PE");

        int index = (ceIndex != -1) ? ceIndex : peIndex;
        if (index == -1 || index < 2) {
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
    public static double getStrikeForTargetDelta(double spot, double targetDelta, double T, double r, double atmIv, boolean isCall) {
        double closestStrike = spot;
        double minDiff = Double.MAX_VALUE;

        for (double strike = spot - 1000; strike <= spot + 1000; strike += 50) {
            double adjustedIv = atmIv;
            if (strike > spot && isCall) adjustedIv += 0.01;
            if (strike < spot && !isCall) adjustedIv += 0.01;

            double delta = isCall
                    ? OptionGreeks.calculateCallDelta(spot, strike, T, r, adjustedIv)
                    : OptionGreeks.calculatePutDelta(spot, strike, T, r, adjustedIv);

            double diff = Math.abs(delta - targetDelta);
            if (diff < minDiff) {
                minDiff = diff;
                closestStrike = strike;
            }

            if (diff < 0.005) break;
        }

        return closestStrike;
    }
    public static void main(String[] args) {
        double spot = 24968.4;      // from "lp"
        double strike = 24950;      // rounded ATM strike
        double timeToExpiry = 3.0 / 365; // manually calculated
        double riskFreeRate = 0.06;
        double iv = 0.15;           // assumption or pulled from external source

        double callDelta = OptionGreeks.calculatePutDelta(spot, strike, timeToExpiry, riskFreeRate, iv);
        System.out.println(findStrikeForTargetDelta(spot,-0.2,timeToExpiry,0.06,0.14,false));
        System.out.println("Call Delta: " + callDelta);
    }
}
