package com.example.swift_app.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyFormatter {

    public static String format(double amount, String currencyCode) {
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance(getLocale(currencyCode));
            format.setCurrency(Currency.getInstance(currencyCode));
            return format.format(amount);
        } catch (Exception e) {
            return getSymbol(currencyCode) + new DecimalFormat("#,##0.00").format(amount);
        }
    }

    public static String formatCompact(double amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000);
        }
        return String.format("%.2f", amount);
    }

    public static String getSymbol(String currencyCode) {
        if (currencyCode == null) return "$";
        switch (currencyCode) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "INR": return "₹";
            case "JPY": return "¥";
            case "PHP": return "₱";
            case "MXN": return "MX$";
            case "BRL": return "R$";
            case "NGN": return "₦";
            case "KES": return "KSh";
            default: return currencyCode + " ";
        }
    }

    private static Locale getLocale(String currencyCode) {
        switch (currencyCode) {
            case "USD": return Locale.US;
            case "EUR": return Locale.GERMANY;
            case "GBP": return Locale.UK;
            case "INR": return new Locale("en", "IN");
            case "JPY": return Locale.JAPAN;
            default: return Locale.US;
        }
    }
}
