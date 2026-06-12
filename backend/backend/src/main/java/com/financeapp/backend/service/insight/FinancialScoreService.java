package com.financeapp.backend.service.insight;

import com.financeapp.backend.dto.FinancialHealthResponse;
import com.financeapp.backend.entity.Expense;

import java.util.List;

public interface FinancialScoreService {

    FinancialHealthResponse calculateFinancialHealth(double totalIncome,
                                                     double totalExpense,
                                                     double monthlyBudget,
                                                     List<Expense> monthlyExpenses);

    FinancialHealthResponse calculateOverallFinancialHealth(double totalIncome,
                                                            double totalExpense,
                                                            List<Object[]> categoryTotals);
}
