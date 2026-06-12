package com.financeapp.backend.controller.insight;

import com.financeapp.backend.dto.FinancialInsightResponse;
import com.financeapp.backend.dto.FinancialHealthResponse;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.insight.InsightService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/monthly")
    public ApiResponse<FinancialInsightResponse> getInsights(
            Authentication authentication,
            @RequestParam int month,
            @RequestParam int year) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Insights generated",
                insightService.generateInsights(username, month, year)
        );
    }

    @GetMapping("/health-score")
    public ApiResponse<FinancialHealthResponse> getFinancialHealth(
            Authentication authentication,
            @RequestParam int month,
            @RequestParam int year) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Financial health calculated",
                insightService.getFinancialHealth(username, month, year)
        );
    }

    @GetMapping("/overall")
    public ApiResponse<FinancialInsightResponse> getOverallInsights(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Overall insights generated",
                insightService.generateOverallInsights(username)
        );
    }

    @GetMapping("/health-score/overall")
    public ApiResponse<FinancialHealthResponse> getOverallFinancialHealth(Authentication authentication) {

        String username = authentication.getName();

        return new ApiResponse<>(
                true,
                "Overall financial health calculated",
                insightService.getOverallFinancialHealth(username)
        );
    }
}