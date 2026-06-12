package com.financeapp.backend.dto.analysis;

import java.time.LocalDateTime;

public class AnalysisOverviewResponse {

    private final HiddenExpenseDetectorResponse hiddenExpenseDetector;
    private final FinancialTwinSimulatorResponse financialTwinSimulator;
    private final LocalDateTime generatedAt;

    public AnalysisOverviewResponse(HiddenExpenseDetectorResponse hiddenExpenseDetector,
                                    FinancialTwinSimulatorResponse financialTwinSimulator,
                                    LocalDateTime generatedAt) {
        this.hiddenExpenseDetector = hiddenExpenseDetector;
        this.financialTwinSimulator = financialTwinSimulator;
        this.generatedAt = generatedAt;
    }

    public HiddenExpenseDetectorResponse getHiddenExpenseDetector() {
        return hiddenExpenseDetector;
    }

    public FinancialTwinSimulatorResponse getFinancialTwinSimulator() {
        return financialTwinSimulator;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
