package com.finance_analysis.dto;

import java.time.LocalDateTime;

public class AuthenticatedUser {

    private final Long id;
    private final String username;
    private final LocalDateTime createdAt;
    private final LocalDateTime issuedAt;

    public AuthenticatedUser(Long id, String username, LocalDateTime createdAt, LocalDateTime issuedAt) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
        this.issuedAt = issuedAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}
