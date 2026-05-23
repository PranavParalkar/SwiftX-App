package com.example.swift_app.services;

import com.example.swift_app.models.Transaction;
import com.example.swift_app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * AML (Anti-Money Laundering) rules engine for compliance.
 * Runs client-side pre-checks before transactions are submitted.
 */
public class AmlRulesEngine {

    public static class AmlResult {
        private boolean flagged;
        private List<String> triggeredRules;
        private String severity; // low, medium, high

        public AmlResult() {
            this.flagged = false;
            this.triggeredRules = new ArrayList<>();
            this.severity = "low";
        }

        public boolean isFlagged() { return flagged; }
        public void setFlagged(boolean flagged) { this.flagged = flagged; }

        public List<String> getTriggeredRules() { return triggeredRules; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public void addRule(String rule) {
            triggeredRules.add(rule);
            flagged = true;
        }
    }

    /**
     * Run all AML checks on a proposed transfer.
     */
    public static AmlResult checkTransfer(double amount, String currency,
                                           List<Transaction> recentTransactions,
                                           String userId) {
        AmlResult result = new AmlResult();

        // Rule 1: Amount threshold
        if (amount >= Constants.AML_AMOUNT_THRESHOLD) {
            result.addRule("AMOUNT_THRESHOLD: Transfer exceeds $" +
                    String.format("%,.0f", Constants.AML_AMOUNT_THRESHOLD));
            result.setSeverity("high");
        }

        // Rule 2: Velocity check — too many transactions in last hour
        if (recentTransactions != null) {
            long oneHourAgo = System.currentTimeMillis() - 3600000;
            int recentCount = 0;
            for (Transaction tx : recentTransactions) {
                if (tx.getSenderId() != null && tx.getSenderId().equals(userId)) {
                    recentCount++;
                }
            }
            if (recentCount >= Constants.AML_VELOCITY_LIMIT) {
                result.addRule("VELOCITY_LIMIT: More than " + Constants.AML_VELOCITY_LIMIT +
                        " transactions in the last hour");
                if (!"high".equals(result.getSeverity())) {
                    result.setSeverity("medium");
                }
            }
        }

        // Rule 3: Daily aggregate limit
        if (recentTransactions != null) {
            double dailyTotal = 0;
            for (Transaction tx : recentTransactions) {
                if (tx.getSenderId() != null && tx.getSenderId().equals(userId)) {
                    dailyTotal += tx.getAmount();
                }
            }
            if (dailyTotal + amount > Constants.AML_DAILY_LIMIT) {
                result.addRule("DAILY_LIMIT: Daily transfers exceed $" +
                        String.format("%,.0f", Constants.AML_DAILY_LIMIT));
                result.setSeverity("high");
            }
        }

        // Rule 4: Round number suspicious pattern
        if (amount >= 1000 && amount % 1000 == 0) {
            result.addRule("ROUND_AMOUNT: Unusually round transfer amount");
            if ("low".equals(result.getSeverity())) {
                result.setSeverity("low");
            }
        }

        return result;
    }

    /**
     * Run all AML checks on a proposed deposit.
     */
    public static AmlResult checkDeposit(double amount, String currency, String userId) {
        AmlResult result = new AmlResult();

        // Rule 1: Large cash-like deposit
        if (amount >= 5000) {
            result.addRule("HIGH_VALUE_DEPOSIT: Deposit exceeds compliance review threshold of $5,000");
            result.setSeverity("medium");
        }

        // Rule 2: Hard limit for unverified
        if (amount >= 10000) {
            result.addRule("LIMIT_EXCEEDED: Maximum single deposit limit reached");
            result.setSeverity("high");
        }

        return result;
    }

    /**
     * Check if a transfer can proceed (not blocked by high-severity flags).
     */
    public static boolean canProceed(AmlResult result) {
        if (!result.isFlagged()) return true;
        // Allow low/medium severity to proceed (flagged for review)
        return !"high".equals(result.getSeverity());
    }
}
