package com.financeapp.backend.service.income;

import com.financeapp.backend.entity.Income;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.IncomeRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public IncomeService(IncomeRepository incomeRepository,
                         UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    // ✅ ADD INCOME (FIXED WITH USER LINKING)
    public Income addIncome(String username,
                            String source,
                            Double amount,
                            LocalDate date) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Income income = new Income();
        income.setSource(source);
        income.setAmount(amount);
        income.setDate(date);
        income.setUser(user);  // 🔥 VERY IMPORTANT

        return incomeRepository.save(income);
    }

    // ✅ GET USER INCOME
    public List<Income> getUserIncome(String username) {
        return incomeRepository.findByUserUsername(username);
    }

    // ✅ TOTAL INCOME
    public Double getTotalIncome(String username) {
        return incomeRepository.findByUserUsername(username)
                .stream()
                .mapToDouble(Income::getAmount)
                .sum();
    }

    // ✅ MONTHLY INCOME
    public Double getMonthlyIncome(String username, int month, int year) {

        return incomeRepository.findByUserUsername(username)
                .stream()
                .filter(i -> i.getDate().getMonthValue() == month
                        && i.getDate().getYear() == year)
                .mapToDouble(Income::getAmount)
                .sum();
    }

    // ✅ INCOME BY DATE RANGE
    public Double getIncomeByDateRange(String username,
                                       LocalDate start,
                                       LocalDate end) {

        return incomeRepository.findByUserUsername(username)
                .stream()
                .filter(i -> !i.getDate().isBefore(start)
                        && !i.getDate().isAfter(end))
                .mapToDouble(Income::getAmount)
                .sum();
    }

    // ✅ SOURCE BREAKDOWN (FOR PIE CHART)
    public Map<String, Double> getSourceBreakdown(String username) {

        return incomeRepository.findByUserUsername(username)
                .stream()
                .collect(Collectors.groupingBy(
                        Income::getSource,
                        Collectors.summingDouble(Income::getAmount)
                ));
    }

    // ✅ UPDATE INCOME
    public Income updateIncome(Long id,
                               String source,
                               Double amount,
                               LocalDate date) {

        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        income.setSource(source);
        income.setAmount(amount);
        income.setDate(date);

        return incomeRepository.save(income);
    }

    // ✅ DELETE INCOME
    public void deleteIncome(Long id) {
        incomeRepository.deleteById(id);
    }
}