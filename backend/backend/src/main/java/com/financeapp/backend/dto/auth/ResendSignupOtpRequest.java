package com.financeapp.backend.dto.auth;

public class ResendSignupOtpRequest {

    private String email;

    public ResendSignupOtpRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
