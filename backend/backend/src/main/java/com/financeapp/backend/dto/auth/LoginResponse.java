package com.financeapp.backend.dto.auth;

public class LoginResponse {

    private final String token;
    private final String tokenType;
    private final long expiresInSeconds;
    private final String username;

    public LoginResponse(String token, String tokenType, long expiresInSeconds, String username) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public String getUsername() {
        return username;
    }
}
