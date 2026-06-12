package com.financeapp.backend.controller.analysis;

import com.financeapp.backend.dto.analysis.AnalysisOverviewResponse;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.analysis.AnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/overview")
    public ApiResponse<AnalysisOverviewResponse> getOverview(Authentication authentication,
                                                             @RequestParam(defaultValue = "2000") double threshold) {
        return new ApiResponse<>(
                true,
                "Analysis overview generated",
                analysisService.getAnalysisOverview(authentication.getName(), threshold)
        );
    }
}
