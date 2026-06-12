package com.financeapp.backend.service.bank;

import org.springframework.web.multipart.MultipartFile;

public interface BankService {

    void processCSV(String username, MultipartFile file);
}
