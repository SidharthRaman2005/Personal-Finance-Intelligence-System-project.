package com.financeapp.backend.controller.report;

import com.financeapp.backend.dto.report.MonthlyReportResponse;
import com.financeapp.backend.dto.report.YearlyReportResponse;
import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.report.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ApiResponse<MonthlyReportResponse> getMonthlyReport(Authentication authentication,
                                                               @RequestParam(required = false) Integer month,
                                                               @RequestParam(required = false) Integer year) {
        YearMonth now = YearMonth.now();
        int targetMonth = month == null ? now.getMonthValue() : month;
        int targetYear = year == null ? now.getYear() : year;

        MonthlyReportResponse report = reportService.getMonthlyReport(
                authentication.getName(),
                targetMonth,
                targetYear
        );

        return new ApiResponse<>(true, "Monthly report generated", report);
    }

    @GetMapping("/yearly")
    public ApiResponse<YearlyReportResponse> getYearlyReport(Authentication authentication,
                                                             @RequestParam(required = false) Integer year) {
        int targetYear = year == null ? YearMonth.now().getYear() : year;
        YearlyReportResponse report = reportService.getYearlyReport(authentication.getName(), targetYear);

        return new ApiResponse<>(true, "Yearly report generated", report);
    }
}
