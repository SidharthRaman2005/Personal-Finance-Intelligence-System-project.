package com.financeapp.backend.controller.bank;

import com.financeapp.backend.response.ApiResponse;
import com.financeapp.backend.service.bank.BankService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    private final BankService bankService;

    // ✅ Constructor Injection (Best Practice)
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // ✅ CSV Upload API
    @PostMapping("/upload")
    public ApiResponse<String> uploadCSV(
            Authentication authentication,
            @RequestParam MultipartFile file
    ) {

        try {

            // 🔒 Validate file
            if (file == null || file.isEmpty()) {
                return new ApiResponse<>(false, "File is empty", null);
            }

            String fileName = file.getOriginalFilename();

            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                return new ApiResponse<>(false, "Only CSV files are allowed", null);
            }

            // 🚀 Process CSV
            bankService.processCSV(authentication.getName(), file);

            return new ApiResponse<>(true, "CSV processed successfully", null);

        } catch (Exception e) {

            e.printStackTrace(); // helpful for debugging

            return new ApiResponse<>(
                    false,
                    "Error processing CSV: " + e.getMessage(),
                    null
            );
        }
    }
}