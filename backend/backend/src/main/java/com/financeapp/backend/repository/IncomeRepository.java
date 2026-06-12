package com.financeapp.backend.repository;

import com.financeapp.backend.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUserUsername(String username);

    List<Income> findByUserUsernameAndDateBetween(
            String username,
            LocalDate start,
            LocalDate end
    );

    @Query("SELECT MONTH(i.date), SUM(i.amount) FROM Income i WHERE i.user.username = :username AND YEAR(i.date) = :year GROUP BY MONTH(i.date) ORDER BY MONTH(i.date)")
    List<Object[]> getMonthlyTotalsByYear(@Param("username") String username,
                                          @Param("year") int year);
}