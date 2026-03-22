package com.fluxbank.auth.application.service.impl;

import com.fluxbank.auth.application.dto.*;
import com.fluxbank.auth.application.mapper.UserMapper;
import com.fluxbank.auth.application.service.AuthService;
import com.fluxbank.auth.domain.event.UserLoggedInEvent;
import com.fluxbank.auth.domain.event.UserRegisteredEvent;
import com.fluxbank.auth.domain.model.RefreshToken;
import com.fluxbank.auth.domain.model.User;
import com.fluxbank.auth.domain.model.UserStatus;
import com.fluxbank.auth.domain.repository.RefreshTokenRepository;
import com.fluxbank.auth.domain.repository.UserRepository;
import com.fluxbank.auth.infrastructure.jwt.JwtTokenProvider;
import com.fluxbank.common.constants.KafkaTopics;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new FluxBankException(ErrorCode.DUPLICATE_REQUEST, "Email address is already registered");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("User registered: userId={}", user.getId());

        String rawRefreshToken = jwtTokenProvider.generateRefreshToken();
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        saveRefreshToken(rawRefreshToken, user, null, null);

        publishEvent(new UserRegisteredEvent(
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        ));

        return buildAuthResponse(accessToken, rawRefreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new FluxBankException(ErrorCode.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new FluxBankException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new FluxBankException(ErrorCode.UNAUTHORIZED, "Account is not active");
        }

        refreshTokenRepository.revokeAllByUser(user);

        String rawRefreshToken = jwtTokenProvider.generateRefreshToken();
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        saveRefreshToken(rawRefreshToken, user, ipAddress, userAgent);

        log.info("User logged in: userId={}", user.getId());

        publishEvent(new UserLoggedInEvent(user.getId().toString(), user.getEmail()));

        return buildAuthResponse(accessToken, rawRefreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String tokenHash = hashToken(request.refreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByToken(tokenHash)
                .orElseThrow(() -> new FluxBankException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new FluxBankException(ErrorCode.UNAUTHORIZED, "Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new FluxBankException(ErrorCode.UNAUTHORIZED, "Refresh token has expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken();
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        saveRefreshToken(rawRefreshToken, user, ipAddress, userAgent);

        log.info("Refresh token rotated: userId={}", user.getId());

        return buildAuthResponse(accessToken, rawRefreshToken, user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        refreshTokenRepository.findByToken(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User logged out: userId={}", token.getUser().getId());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "User not found"));
        return userMapper.toDto(user);
    }

    private void saveRefreshToken(String rawToken, User user, String ipAddress, String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(hashToken(rawToken))
                .user(user)
                .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpiryMs()))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(String accessToken, String rawRefreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
                .user(userMapper.toDto(user))
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void publishEvent(Object event) {
        try {
            if (event instanceof UserRegisteredEvent e) {
                kafkaTemplate.send(KafkaTopics.AUTH_EVENTS, e.getAggregateId(), event);
            } else if (event instanceof UserLoggedInEvent e) {
                kafkaTemplate.send(KafkaTopics.AUTH_EVENTS, e.getAggregateId(), event);
            }
        } catch (Exception ex) {
            log.error("Failed to publish event: {}", ex.getMessage());
        }
    }
}
