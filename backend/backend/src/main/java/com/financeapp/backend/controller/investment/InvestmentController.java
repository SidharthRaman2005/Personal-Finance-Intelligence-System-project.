package com.financeapp.backend.controller.investment;

import com.financeapp.backend.dto.investment.InvestmentRequest;
import com.financeapp.backend.dto.investment.InvestmentResponse;
import com.financeapp.backend.dto.investment.InvestmentSummaryResponse;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.investment.InvestmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping("/add")
    public ApiResponse<InvestmentResponse> addInvestment(Authentication authentication,
                                                         @RequestBody InvestmentRequest request) {
        InvestmentResponse response = investmentService.createInvestment(authentication.getName(), request);
        return new ApiResponse<>(true, "Investment added", response);
    }

    @GetMapping
    public ApiResponse<List<InvestmentResponse>> getInvestments(Authentication authentication) {
        return new ApiResponse<>(
                true,
                "Investments fetched",
                investmentService.getInvestments(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<InvestmentResponse> getInvestment(Authentication authentication,
                                                         @PathVariable Long id) {
        return new ApiResponse<>(
                true,
                "Investment fetched",
                investmentService.getInvestment(authentication.getName(), id)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<InvestmentResponse> updateInvestment(Authentication authentication,
                                                            @PathVariable Long id,
                                                            @RequestBody InvestmentRequest request) {
        return new ApiResponse<>(
                true,
                "Investment updated",
                investmentService.updateInvestment(authentication.getName(), id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteInvestment(Authentication authentication,
                                                 @PathVariable Long id) {
        investmentService.deleteInvestment(authentication.getName(), id);
        return new ApiResponse<>(true, "Investment deleted", null);
    }

    @GetMapping("/summary")
    public ApiResponse<InvestmentSummaryResponse> getSummary(Authentication authentication) {
        return new ApiResponse<>(
                true,
                "Investment summary fetched",
                investmentService.getSummary(authentication.getName())
        );
    }
}
