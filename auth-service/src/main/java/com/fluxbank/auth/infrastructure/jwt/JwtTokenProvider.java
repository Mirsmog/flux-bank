package com.fluxbank.auth.infrastructure.jwt;

import com.fluxbank.auth.domain.model.User;
import com.fluxbank.auth.infrastructure.config.JwtConfig;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final ResourceLoader resourceLoader;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @PostConstruct
    public void init() {
        try {
            privateKey = loadPrivateKey(jwtConfig.getPrivateKeyPath());
            publicKey = loadPublicKey(jwtConfig.getPublicKeyPath());
            log.info("JWT RSA keys loaded successfully");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JWT RSA keys", e);
        }
    }

    /**
     * Generates a signed RS256 access token for the given user.
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpiryMs());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("type", "ACCESS")
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    /**
     * Generates a cryptographically secure random 32-byte hex string.
     * This is NOT a JWT — it is stored hashed (SHA-256) in the database.
     */
    public String generateRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Validates the given JWT token and returns its claims.
     *
     * @throws FluxBankException with UNAUTHORIZED error code if the token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            throw new FluxBankException(ErrorCode.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    /**
     * Extracts the subject (userId) from a JWT token without re-validating.
     * Only call after {@link #validateToken(String)} has succeeded.
     */
    public String getSubject(String token) {
        return validateToken(token).getSubject();
    }

    public long getAccessTokenExpiryMs() {
        return jwtConfig.getAccessTokenExpiryMs();
    }

    public long getRefreshTokenExpiryMs() {
        return jwtConfig.getRefreshTokenExpiryMs();
    }

    private RSAPrivateKey loadPrivateKey(String resourcePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = new String(resourceLoader.getResource(resourcePath).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String keyData = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(keyData);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private RSAPublicKey loadPublicKey(String resourcePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = new String(resourceLoader.getResource(resourcePath).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String keyData = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(keyData);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }
}
