package com.example.swift_app.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.example.swift_app.models.Transaction;

/**
 * Hash chain service for maintaining transaction integrity.
 * Each transaction's current_hash depends on the previous transaction's hash,
 * creating a tamper-evident chain similar to a simplified blockchain.
 */
public class HashChainService {

    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    /**
     * Computes the hash for a transaction based on its deterministic fields and the previous hash.
     */
    public static String computeHash(Transaction tx, String prevHash) {
        String input = buildHashInput(tx, prevHash != null ? prevHash : GENESIS_HASH);
        return sha256(input);
    }

    /**
     * Verifies that a transaction's stored hash matches the computed hash.
     */
    public static boolean verifyTransaction(Transaction tx) {
        String computed = computeHash(tx, tx.getPrevHash());
        return computed.equals(tx.getCurrentHash());
    }

    /**
     * Verifies an entire chain of transactions.
     */
    public static boolean verifyChain(java.util.List<Transaction> chain) {
        String prevHash = GENESIS_HASH;
        for (Transaction tx : chain) {
            // Check prev_hash linkage
            if (tx.getPrevHash() != null && !tx.getPrevHash().equals(prevHash)) {
                return false;
            }
            // Check current_hash integrity
            String computed = computeHash(tx, prevHash);
            if (!computed.equals(tx.getCurrentHash())) {
                return false;
            }
            prevHash = tx.getCurrentHash();
        }
        return true;
    }

    /**
     * Gets the hash of the latest transaction in a chain (the chain root).
     */
    public static String getChainRoot(java.util.List<Transaction> chain) {
        if (chain == null || chain.isEmpty()) return GENESIS_HASH;
        Transaction last = chain.get(chain.size() - 1);
        return last.getCurrentHash() != null ? last.getCurrentHash() : GENESIS_HASH;
    }

    public static String getGenesisHash() {
        return GENESIS_HASH;
    }

    private static String buildHashInput(Transaction tx, String prevHash) {
        StringBuilder sb = new StringBuilder();
        sb.append(prevHash);
        sb.append("|").append(tx.getSenderId() != null ? tx.getSenderId() : "");
        sb.append("|").append(tx.getRecipientId() != null ? tx.getRecipientId() : "");
        sb.append("|").append(String.format("%.2f", tx.getAmount()));
        sb.append("|").append(tx.getCurrency() != null ? tx.getCurrency() : "");
        sb.append("|").append(tx.getType() != null ? tx.getType() : "");
        sb.append("|").append(tx.getCreatedAt() != null ? tx.getCreatedAt() : "");
        return sb.toString();
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
