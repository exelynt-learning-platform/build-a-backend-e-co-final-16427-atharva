package com.ecommerce.auth;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${jwt.secret.min.length:32}")
    private int jwtSecretMinLength;

    @PostConstruct
    public void validateJwtSecret() {
        if (!StringUtils.hasText(jwtSecret)) {
            logger.error("JWT secret is not configured. Set JWT_SECRET environment variable with at least {} characters.", jwtSecretMinLength);
            logger.error("For development, create a .env file with: JWT_SECRET=your-{}-character-secret-key", jwtSecretMinLength);
            logger.error("Application will continue with limited functionality for development purposes.");
            // Don't throw exception in development to allow testing without JWT
            return;
        }
        if (jwtSecret.length() < jwtSecretMinLength) {
            logger.error("JWT secret is too short ({} characters). Minimum required: {} characters.", jwtSecret.length(), jwtSecretMinLength);
            logger.error("Please use a longer secret for production security.");
            // Don't throw exception in development to allow testing with short secrets
            return;
        }
        logger.info("JWT configuration validated successfully ({} characters)", jwtSecret.length());
    }

    public String generateJwtToken(Authentication authentication) {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT secret is not configured. Cannot generate token without proper security configuration.");
        }
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String token) {
        if (!StringUtils.hasText(jwtSecret)) {
            logger.warn("JWT secret is not configured. Token validation will fail.");
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT token claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
