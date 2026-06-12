package com.financeapp.backend.bank.util;

import com.financeapp.backend.bank.model.BankTransaction;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    public static List<BankTransaction> parse(MultipartFile file) {

        List<BankTransaction> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");

                // 🔒 VALIDATION
                if (data.length < 4) continue;

                try {
                    LocalDate date;
                    String dateStr = data[0].trim();
                    try {
                        // Try ISO first
                        date = LocalDate.parse(dateStr);
                    } catch (Exception ex1) {
                        // Try dd-MM-yyyy
                        try {
                            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            date = LocalDate.parse(dateStr, dtf);
                        } catch (Exception ex2) {
                            System.out.println("Skipping invalid date: " + dateStr);
                            continue;
                        }
                    }
                    String description = data[1].trim();
                    Double amount = Double.parseDouble(data[2].trim());
                    String type = data[3].trim();

                    list.add(new BankTransaction(date, description, amount, type));

                } catch (Exception e) {
                    System.out.println("Skipping invalid row: " + line);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV: " + e.getMessage());
        }

        return list;
    }
}