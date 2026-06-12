package com.financeapp.backend.bank.util;

import java.util.HashMap;
import java.util.Map;

public class CategoryUtil {

    private static final double SMALL_AMOUNT_THRESHOLD = 100.0;
    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();

    static {
        // Food
        CATEGORY_MAP.put("swiggy", "Food");
        CATEGORY_MAP.put("zomato", "Food");
        CATEGORY_MAP.put("restaurant", "Food");
        CATEGORY_MAP.put("cafe", "Food");
        CATEGORY_MAP.put("dining", "Food");
        CATEGORY_MAP.put("grocery", "Food");
        CATEGORY_MAP.put("blinkit", "Food");
        CATEGORY_MAP.put("instamart", "Food");

        // Transport
        CATEGORY_MAP.put("uber", "Transport");
        CATEGORY_MAP.put("ola", "Transport");
        CATEGORY_MAP.put("petrol", "Transport");
        CATEGORY_MAP.put("diesel", "Transport");
        CATEGORY_MAP.put("fuel", "Transport");
        CATEGORY_MAP.put("metro", "Transport");
        CATEGORY_MAP.put("cab", "Transport");

        // Shopping
        CATEGORY_MAP.put("amazon", "Shopping");
        CATEGORY_MAP.put("flipkart", "Shopping");
        CATEGORY_MAP.put("myntra", "Shopping");
        CATEGORY_MAP.put("ajio", "Shopping");
        CATEGORY_MAP.put("meesho", "Shopping");

        // Income
        CATEGORY_MAP.put("salary", "Income");
        CATEGORY_MAP.put("bonus", "Income");
        CATEGORY_MAP.put("freelance", "Income");
        CATEGORY_MAP.put("refund", "Income");

        // Housing & utilities
        CATEGORY_MAP.put("rent", "Rent");
        CATEGORY_MAP.put("electricity", "Utilities");
        CATEGORY_MAP.put("water", "Utilities");
        CATEGORY_MAP.put("wifi", "Utilities");
        CATEGORY_MAP.put("internet", "Utilities");
        CATEGORY_MAP.put("recharge", "Utilities");

        // Health & entertainment
        CATEGORY_MAP.put("pharmacy", "Healthcare");
        CATEGORY_MAP.put("hospital", "Healthcare");
        CATEGORY_MAP.put("doctor", "Healthcare");
        CATEGORY_MAP.put("medicine", "Healthcare");
        CATEGORY_MAP.put("netflix", "Entertainment");
        CATEGORY_MAP.put("spotify", "Entertainment");
        CATEGORY_MAP.put("movie", "Entertainment");
    }

    public static String categorize(String description) {
        return categorize(description, null);
    }

    public static String categorize(String description, Double amount) {

        if (description == null || description.isBlank()) {
            return isSmallAmount(amount) ? "Misc" : "Others";
        }

        String lower = description.toLowerCase();

        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        if (isSmallAmount(amount)) {
            return "Misc";
        }

        return "Others";
    }

    public static void registerKeyword(String keyword, String category) {
        if (keyword == null || keyword.isBlank() || category == null || category.isBlank()) {
            return;
        }

        CATEGORY_MAP.put(keyword.toLowerCase().trim(), category.trim());
    }

    private static boolean isSmallAmount(Double amount) {
        return amount != null && Math.abs(amount) <= SMALL_AMOUNT_THRESHOLD;
    }
}