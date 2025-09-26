package com.finance_analysis.service;

import java.time.LocalDateTime;


import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.finance_analysis.dto.AuthResponse;
import com.finance_analysis.dto.AuthenticatedUser;
import com.finance_analysis.dto.LoginRequest;
import com.finance_analysis.dto.RegisterRequest;
import com.finance_analysis.dto.UserDto;
import com.finance_analysis.exception.UnauthorizedException;
import com.finance_analysis.mapper.UserMapper;
import com.finance_analysis.model.User;

@Service
public class AuthService {

    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       AuthTokenService authTokenService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (userMapper.findByUsername(username) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already registered");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        String token = authTokenService.issueToken(user);
        return new AuthResponse(token, toDto(user));
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        String token = authTokenService.issueToken(user);
        return new AuthResponse(token, toDto(user));
    }

    public UserDto requireUser(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        AuthenticatedUser authenticated = authTokenService.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));
        return new UserDto(authenticated.getId(), authenticated.getUsername(), authenticated.getCreatedAt());
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        return normalized;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Missing Authorization header");
        }
        String header = authorizationHeader.trim();
        if (header.regionMatches(true, 0, AUTHORIZATION_PREFIX, 0, AUTHORIZATION_PREFIX.length())) {
            return header.substring(AUTHORIZATION_PREFIX.length()).trim();
        }
        throw new UnauthorizedException("Invalid Authorization header");
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getCreatedAt());
    }
}
