package com.financeapp.backend.entity;

import java.util.Locale;

public enum InvestmentType {
    STOCKS("Stocks"),
    MUTUAL_FUNDS("Mutual Funds"),
    FIXED_DEPOSITS("Fixed Deposits"),
    GOLD("Gold"),
    CRYPTO("Crypto"),
    REAL_ESTATE("Real Estate"),
    PPF("PPF"),
    EPF("EPF"),
    BONDS("Bonds"),
    SAVINGS("Savings");

    private final String label;

    InvestmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static InvestmentType fromLabel(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Investment type is required");
        }

        String normalized = raw.trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace(" ", "_");

        return InvestmentType.valueOf(normalized);
    }
}
