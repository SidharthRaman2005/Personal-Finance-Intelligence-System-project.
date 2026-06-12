package com.financeapp.backend.service.bank;

import com.financeapp.backend.bank.model.BankTransaction;
import com.financeapp.backend.bank.util.CSVParser;
import com.financeapp.backend.bank.util.CategoryUtil;
import com.financeapp.backend.entity.Expense;
import com.financeapp.backend.entity.Income;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.ExpenseRepository;
import com.financeapp.backend.repository.IncomeRepository;
import com.financeapp.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BankServiceImpl implements BankService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void processCSV(String username, MultipartFile file) {

        // 🔒 Validate user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BankTransaction> transactions = CSVParser.parse(file);

        for (BankTransaction tx : transactions) {

            try {

                // 🔒 Skip invalid
                if (tx.getAmount() == null || tx.getType() == null) continue;

                String category = CategoryUtil.categorize(tx.getDescription(), tx.getAmount());

                if (tx.getType().equalsIgnoreCase("DEBIT")) {

                        Expense expense = new Expense();
                        // Set title to description or fallback
                        expense.setTitle(tx.getDescription() != null ? tx.getDescription() : "Bank Transaction");
                        expense.setDescription(tx.getDescription());
                        expense.setAmount(Math.abs(tx.getAmount()));
                        expense.setCategory(category);
                        expense.setDate(tx.getDate()); // ✅ FIXED
                        expense.setUser(user);

                        expenseRepository.save(expense);

                } else if (tx.getType().equalsIgnoreCase("CREDIT")) {

                    Income income = new Income();
                    income.setSource(tx.getDescription());
                    income.setAmount(tx.getAmount());
                    income.setDate(tx.getDate()); // ✅ FIXED
                    income.setUser(user);

                    incomeRepository.save(income);
                }

            } catch (Exception e) {
                System.out.println("Skipping failed transaction: " + tx);
            }
        }
    }
}