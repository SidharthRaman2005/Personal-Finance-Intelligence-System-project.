package com.financeapp.backend.service.analysis;

import com.financeapp.backend.dto.analysis.AnalysisOverviewResponse;

public interface AnalysisService {
    AnalysisOverviewResponse getAnalysisOverview(String username, double threshold);
}
