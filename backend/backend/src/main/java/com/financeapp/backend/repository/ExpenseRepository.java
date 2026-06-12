package com.financeapp.backend.repository;

import com.financeapp.backend.entity.Expense;
import com.financeapp.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ✅ Get all expenses for a user
    List<Expense> findByUser(User user);

    // ✅ Get expenses for a specific month and year
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    List<Expense> findMonthlyExpenses(@Param("user") User user,
                                      @Param("month") int month,
                                      @Param("year") int year);

    // ✅ Category totals
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user = :user GROUP BY e.category")
    List<Object[]> getCategoryTotals(@Param("user") User user);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user = :user AND MONTH(e.date) = :month AND YEAR(e.date) = :year GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getMonthlyCategoryTotals(@Param("user") User user,
                                            @Param("month") int month,
                                            @Param("year") int year);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user = :user AND YEAR(e.date) = :year GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getYearlyCategoryTotals(@Param("user") User user,
                                           @Param("year") int year);

    // ✅ Total expense of user
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user")
    Double getTotalExpenseByUser(@Param("user") User user);

    // ✅ Expense count
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user = :user")
    Long countByUser(@Param("user") User user);

    // ✅ This month expense
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.date BETWEEN :start AND :end")
    Double getTotalExpenseByUserAndDateBetween(@Param("user") User user,
                                              @Param("start") LocalDate start,
                                              @Param("end") LocalDate end);

    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.date BETWEEN :start AND :end")
    List<Expense> findByUserAndDateBetween(@Param("user") User user,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("SELECT MONTH(e.date), SUM(e.amount) FROM Expense e WHERE e.user = :user AND YEAR(e.date) = :year GROUP BY MONTH(e.date) ORDER BY MONTH(e.date)")
    List<Object[]> getMonthlyTotalsByYear(@Param("user") User user,
                                          @Param("year") int year);

    // ✅ Top category
    @Query("SELECT e.category FROM Expense e WHERE e.user = :user GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<String> findTopCategory(@Param("user") User user);
}