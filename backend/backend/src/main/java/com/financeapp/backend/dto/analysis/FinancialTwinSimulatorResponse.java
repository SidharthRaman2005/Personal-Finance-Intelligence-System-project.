package com.financeapp.backend.dto.analysis;

import java.util.List;

public class FinancialTwinSimulatorResponse {

    private final double averageMonthlyIncome;
    private final double averageMonthlyExpense;
    private final double averageMonthlySavings;
    private final ProjectionResponse currentPath;
    private final ProjectionResponse optimizedPath;
    private final ProjectionResponse savingsGrowthPath;
    private final double projectedGainFromOptimization;
    private final String currentStrategyInsight;
    private final String improvedStrategyInsight;
    private final List<ProjectionPoint> projectionSeries;

    public FinancialTwinSimulatorResponse(double averageMonthlyIncome,
                                          double averageMonthlyExpense,
                                          double averageMonthlySavings,
                                          ProjectionResponse currentPath,
                                          ProjectionResponse optimizedPath,
                                          ProjectionResponse savingsGrowthPath,
                                          double projectedGainFromOptimization,
                                          String currentStrategyInsight,
                                          String improvedStrategyInsight,
                                          List<ProjectionPoint> projectionSeries) {
        this.averageMonthlyIncome = averageMonthlyIncome;
        this.averageMonthlyExpense = averageMonthlyExpense;
        this.averageMonthlySavings = averageMonthlySavings;
        this.currentPath = currentPath;
        this.optimizedPath = optimizedPath;
        this.savingsGrowthPath = savingsGrowthPath;
        this.projectedGainFromOptimization = projectedGainFromOptimization;
        this.currentStrategyInsight = currentStrategyInsight;
        this.improvedStrategyInsight = improvedStrategyInsight;
        this.projectionSeries = projectionSeries;
    }

    public double getAverageMonthlyIncome() {
        return averageMonthlyIncome;
    }

    public double getAverageMonthlyExpense() {
        return averageMonthlyExpense;
    }

    public double getAverageMonthlySavings() {
        return averageMonthlySavings;
    }

    public ProjectionResponse getCurrentPath() {
        return currentPath;
    }

    public ProjectionResponse getOptimizedPath() {
        return optimizedPath;
    }

    public ProjectionResponse getSavingsGrowthPath() {
        return savingsGrowthPath;
    }

    public double getProjectedGainFromOptimization() {
        return projectedGainFromOptimization;
    }

    public String getCurrentStrategyInsight() {
        return currentStrategyInsight;
    }

    public String getImprovedStrategyInsight() {
        return improvedStrategyInsight;
    }

    public List<ProjectionPoint> getProjectionSeries() {
        return projectionSeries;
    }

    public static class ProjectionResponse {
        private final String scenario;
        private final double oneYearSavings;
        private final double threeYearSavings;
        private final double fiveYearSavings;

        public ProjectionResponse(String scenario,
                                  double oneYearSavings,
                                  double threeYearSavings,
                                  double fiveYearSavings) {
            this.scenario = scenario;
            this.oneYearSavings = oneYearSavings;
            this.threeYearSavings = threeYearSavings;
            this.fiveYearSavings = fiveYearSavings;
        }

        public String getScenario() {
            return scenario;
        }

        public double getOneYearSavings() {
            return oneYearSavings;
        }

        public double getThreeYearSavings() {
            return threeYearSavings;
        }

        public double getFiveYearSavings() {
            return fiveYearSavings;
        }
    }

    public static class ProjectionPoint {
        private final int year;
        private final double currentPathSavings;
        private final double optimizedPathSavings;
        private final double savingsGrowthPathSavings;

        public ProjectionPoint(int year,
                               double currentPathSavings,
                               double optimizedPathSavings,
                               double savingsGrowthPathSavings) {
            this.year = year;
            this.currentPathSavings = currentPathSavings;
            this.optimizedPathSavings = optimizedPathSavings;
            this.savingsGrowthPathSavings = savingsGrowthPathSavings;
        }

        public int getYear() {
            return year;
        }

        public double getCurrentPathSavings() {
            return currentPathSavings;
        }

        public double getOptimizedPathSavings() {
            return optimizedPathSavings;
        }

        public double getSavingsGrowthPathSavings() {
            return savingsGrowthPathSavings;
        }
    }
}
