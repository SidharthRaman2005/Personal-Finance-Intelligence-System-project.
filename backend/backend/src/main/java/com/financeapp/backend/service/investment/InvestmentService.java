package com.financeapp.backend.service.investment;

import com.financeapp.backend.dto.investment.InvestmentRequest;
import com.financeapp.backend.dto.investment.InvestmentResponse;
import com.financeapp.backend.dto.investment.InvestmentSummaryResponse;
import com.financeapp.backend.entity.Investment;
import com.financeapp.backend.entity.InvestmentType;
import com.financeapp.backend.entity.User;
import com.financeapp.backend.repository.InvestmentRepository;
import com.financeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    public InvestmentService(InvestmentRepository investmentRepository,
                             UserRepository userRepository) {
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
    }

    public InvestmentResponse createInvestment(String username, InvestmentRequest request) {
        if (request == null) {
            throw new RuntimeException("Investment details are required");
        }

        InvestmentType type = parseType(request.getInvestmentType());
        String name = safeTrim(request.getInvestmentName());

        if (name == null) {
            throw new RuntimeException("Investment name is required");
        }

        validateInvestedAmount(request.getInvestedAmount());

        Double currentValue = request.getCurrentValue();
        if (currentValue == null) {
            currentValue = request.getInvestedAmount();
        }

        validateCurrentValue(currentValue);

        if (request.getPurchaseDate() == null) {
            throw new RuntimeException("Purchase date is required");
        }

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Investment investment = new Investment(
                type,
                name,
                request.getInvestedAmount(),
                currentValue,
                request.getPurchaseDate(),
                safeTrim(request.getNotes()),
                safeTrim(request.getRiskLevel()),
                user
        );

        return toResponse(investmentRepository.save(investment));
    }

    public List<InvestmentResponse> getInvestments(String username) {
        return investmentRepository.findByUserUsername(username)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public InvestmentResponse getInvestment(String username, Long id) {
        Investment investment = investmentRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
        return toResponse(investment);
    }

    public InvestmentResponse updateInvestment(String username, Long id, InvestmentRequest request) {
        if (request == null) {
            throw new RuntimeException("Investment details are required");
        }

        Investment investment = investmentRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        if (request.getInvestmentType() != null && !request.getInvestmentType().trim().isEmpty()) {
            investment.setInvestmentType(parseType(request.getInvestmentType()));
        }

        String name = safeTrim(request.getInvestmentName());
        if (name != null) {
            investment.setInvestmentName(name);
        }

        Double investedAmount = request.getInvestedAmount();
        Double currentValue = request.getCurrentValue();

        if (investedAmount != null) {
            validateInvestedAmount(investedAmount);
        }

        if (currentValue != null) {
            validateCurrentValue(currentValue);
        }

        if (investedAmount != null) {
            investment.setInvestedAmount(investedAmount);
        }

        if (currentValue != null) {
            investment.setCurrentValue(currentValue);
        } else if (investedAmount != null && investment.getCurrentValue() == null) {
            investment.setCurrentValue(investedAmount);
        }

        if (request.getPurchaseDate() != null) {
            investment.setPurchaseDate(request.getPurchaseDate());
        }

        if (request.getNotes() != null) {
            investment.setNotes(safeTrim(request.getNotes()));
        }

        if (request.getRiskLevel() != null) {
            investment.setRiskLevel(safeTrim(request.getRiskLevel()));
        }

        return toResponse(investmentRepository.save(investment));
    }

    public void deleteInvestment(String username, Long id) {
        Investment investment = investmentRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
        investmentRepository.delete(investment);
    }

    public InvestmentSummaryResponse getSummary(String username) {
        List<Investment> investments = investmentRepository.findByUserUsername(username);

        double totalInvested = investments.stream()
                .mapToDouble(inv -> inv.getInvestedAmount() == null ? 0.0 : inv.getInvestedAmount())
                .sum();

        double totalCurrent = investments.stream()
                .mapToDouble(inv -> inv.getCurrentValue() == null ? 0.0 : inv.getCurrentValue())
                .sum();

        double profitLoss = totalCurrent - totalInvested;

        Map<InvestmentType, Double> totalsByType = new LinkedHashMap<>();
        for (Investment investment : investments) {
            if (investment.getInvestmentType() == null) {
                continue;
            }
            double value = investment.getCurrentValue() == null ? 0.0 : investment.getCurrentValue();
            totalsByType.merge(investment.getInvestmentType(), value, Double::sum);
        }

        Map<String, Double> allocation = new LinkedHashMap<>();
        if (totalCurrent > 0) {
            totalsByType.entrySet().stream()
                    .sorted(Map.Entry.<InvestmentType, Double>comparingByValue().reversed())
                    .forEach(entry -> {
                        double percent = (entry.getValue() / totalCurrent) * 100.0;
                        allocation.put(entry.getKey().getLabel(), percent);
                    });
        }

        double diversificationScore = calculateDiversificationScore(totalCurrent, totalsByType);

        String highestCategory = totalsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().getLabel())
                .orElse(null);

        List<String> insights = buildInsights(allocation, diversificationScore, totalCurrent, profitLoss);

        return new InvestmentSummaryResponse(
                totalInvested,
                totalCurrent,
                profitLoss,
                allocation,
                diversificationScore,
                highestCategory,
                insights
        );
    }

    private InvestmentType parseType(String rawType) {
        try {
            return InvestmentType.fromLabel(rawType);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Unsupported investment type");
        }
    }

    private void validateInvestedAmount(Double investedAmount) {
        if (investedAmount == null) {
            throw new RuntimeException("Invested amount is required");
        }
        if (investedAmount <= 0) {
            throw new RuntimeException("Invested amount must be greater than 0");
        }
    }

    private void validateCurrentValue(Double currentValue) {
        if (currentValue == null) {
            return;
        }
        if (currentValue < 0) {
            throw new RuntimeException("Amounts cannot be negative");
        }
    }

    private double calculateDiversificationScore(double totalCurrent, Map<InvestmentType, Double> totalsByType) {
        if (totalCurrent <= 0) {
            return 0.0;
        }

        double hhi = totalsByType.values()
                .stream()
                .mapToDouble(value -> {
                    double weight = value / totalCurrent;
                    return weight * weight;
                })
                .sum();

        double score = (1.0 - hhi) * 100.0;
        return Math.max(0.0, Math.min(100.0, score));
    }

    private List<String> buildInsights(Map<String, Double> allocation,
                                       double diversificationScore,
                                       double totalCurrent,
                                       double profitLoss) {
        List<String> insights = new ArrayList<>();

        if (totalCurrent <= 0) {
            insights.add("No investments yet. Start with a low-risk savings or fixed deposit.");
            return insights;
        }

        double stocksShare = allocation.getOrDefault("Stocks", 0.0);
        if (stocksShare >= 70.0) {
            insights.add("Your portfolio is highly equity-focused and may carry higher risk.");
        }

        double realEstateShare = allocation.getOrDefault("Real Estate", 0.0);
        if (realEstateShare >= 50.0) {
            insights.add("Most of your money is locked in illiquid assets.");
        }

        double savingsShare = allocation.getOrDefault("Savings", 0.0);
        if (savingsShare > 0 && savingsShare < 8.0) {
            insights.add("Consider increasing liquid emergency savings.");
        }

        if (diversificationScore >= 60.0 && allocation.size() >= 4) {
            insights.add("Your portfolio is well diversified.");
        }

        if (profitLoss < 0) {
            insights.add("Your portfolio is currently below the invested amount.");
        } else if (profitLoss > 0) {
            insights.add("Your portfolio value is above the invested amount.");
        }

        if (insights.isEmpty()) {
            insights.add("Your portfolio is stable. Track allocation regularly.");
        }

        return insights;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private InvestmentResponse toResponse(Investment investment) {
        String typeLabel = investment.getInvestmentType() == null
                ? null
                : investment.getInvestmentType().getLabel();

        return new InvestmentResponse(
                investment.getId(),
                typeLabel,
                investment.getInvestmentName(),
                investment.getInvestedAmount(),
                investment.getCurrentValue(),
                investment.getPurchaseDate(),
                investment.getNotes(),
                investment.getRiskLevel()
        );
    }
}
