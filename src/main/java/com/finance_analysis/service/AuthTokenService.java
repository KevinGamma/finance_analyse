package com.finance_analysis.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.finance_analysis.dto.AuthenticatedUser;
import com.finance_analysis.model.User;

@Service
public class AuthTokenService {

    private final Map<String, AuthenticatedUser> activeTokens = new ConcurrentHashMap<>();

    public String issueToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime issuedAt = LocalDateTime.now();
        activeTokens.put(token, new AuthenticatedUser(user.getId(), user.getUsername(), user.getCreatedAt(), issuedAt));
        return token;
    }

    public Optional<AuthenticatedUser> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String normalized = token.trim();
        return Optional.ofNullable(activeTokens.get(normalized));
    }

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        activeTokens.remove(token.trim());
    }
}
