package com.fluxbank.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String privateKeyPath = "classpath:keys/private_key.pem";
    private String publicKeyPath = "classpath:keys/public_key.pem";
    private long accessTokenExpiryMs = 900_000L;
    private long refreshTokenExpiryMs = 604_800_000L;
    private String issuer = "flux-bank";
}
