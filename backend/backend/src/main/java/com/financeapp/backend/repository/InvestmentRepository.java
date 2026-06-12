package com.financeapp.backend.repository;

import com.financeapp.backend.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserUsername(String username);

    Optional<Investment> findByIdAndUserUsername(Long id, String username);
}
