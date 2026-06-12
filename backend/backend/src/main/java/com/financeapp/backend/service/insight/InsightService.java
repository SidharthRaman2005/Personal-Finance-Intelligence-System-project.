package com.financeapp.backend.service.insight;

import com.financeapp.backend.dto.FinancialInsightResponse;
import com.financeapp.backend.dto.FinancialHealthResponse;

public interface InsightService {

    FinancialInsightResponse generateInsights(
            String username,
            int month,
            int year
    );

    FinancialHealthResponse getFinancialHealth(
            String username,
            int month,
            int year
    );

        FinancialInsightResponse generateOverallInsights(String username);

        FinancialHealthResponse getOverallFinancialHealth(String username);
}