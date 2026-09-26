package com.example.user_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String secretKey;

    public JwtService(
            @Value("${jwt.secret}") String secretKey) {

        this.secretKey = secretKey;
    }

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    // =====================================================
    // GENERATE JWT
    // =====================================================

    public String generateToken(
            String username,
            String role) {

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000L * 60 * 60
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    // =====================================================
    // EXTRACT USERNAME
    // =====================================================

    public String extractUsername(
            String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // =====================================================
    // EXTRACT ROLE
    // =====================================================

    public String extractRole(
            String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isValid(
            String token) {

        try {

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // =====================================================
    // VALIDATE TOKEN + USERNAME
    // =====================================================

    public boolean isTokenValid(
            String token,
            String username) {

        try {

            return username.equals(
                    extractUsername(token)
            )
                    && isValid(token);

        } catch (Exception e) {

            return false;
        }
    }
}