package com.fluxbank.auth.application.service;

import com.fluxbank.auth.application.dto.*;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);
    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent);
    void logout(String refreshToken);
    UserDto getCurrentUser(UUID userId);
}
