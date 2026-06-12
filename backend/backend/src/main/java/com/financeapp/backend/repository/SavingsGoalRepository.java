package com.financeapp.backend.repository;

import com.financeapp.backend.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserUsername(String username);

    Optional<SavingsGoal> findByIdAndUserUsername(Long id, String username);

}