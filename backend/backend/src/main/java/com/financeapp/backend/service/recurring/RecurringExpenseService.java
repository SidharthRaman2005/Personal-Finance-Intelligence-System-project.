package com.financeapp.backend.service.recurring;

import com.financeapp.backend.entity.*;
import com.financeapp.backend.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public RecurringExpenseService(RecurringExpenseRepository recurringRepository,
                                   ExpenseRepository expenseRepository,
                                   UserRepository userRepository) {
        this.recurringRepository = recurringRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public void addRecurringExpense(String username,
                                    String title,
                                    Double amount,
                                    String category,
                                    LocalDate startDate,
                                    LocalDate nextExecutionDate,
                                    Frequency frequency) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RecurringExpense recurring = new RecurringExpense();
        recurring.setTitle(title);
        recurring.setAmount(amount);
        recurring.setCategory(category);
        recurring.setStartDate(startDate);
        recurring.setNextExecutionDate(nextExecutionDate);
        recurring.setFrequency(frequency);
        recurring.setUser(user);

        recurringRepository.save(recurring);
    }

    public void generateRecurringExpenses() {

        LocalDate today = LocalDate.now();
        List<RecurringExpense> list =
                recurringRepository.findByNextExecutionDateLessThanEqual(today);

        for (RecurringExpense recurring : list) {

            Expense expense = new Expense();
            expense.setTitle(recurring.getTitle());
            expense.setAmount(recurring.getAmount());
            expense.setCategory(recurring.getCategory());
            expense.setDate(today);
            expense.setUser(recurring.getUser());

            expenseRepository.save(expense);

            LocalDate nextDate = calculateNextDate(recurring);
            recurring.setNextExecutionDate(nextDate);
            recurringRepository.save(recurring);
        }
    }

    private LocalDate calculateNextDate(RecurringExpense recurring) {

        LocalDate current = recurring.getNextExecutionDate();

        return switch (recurring.getFrequency()) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
        };
    }
}