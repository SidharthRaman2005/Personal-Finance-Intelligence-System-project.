package com.financeapp.backend.dto.auth;

public class VerifySignupOtpRequest {

    private String email;
    private String otp;

    public VerifySignupOtpRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
