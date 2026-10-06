package com.deepak.paymentService.security;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String SECRET_KEY =
            "mySecretKeyForMacyEcommerceApplication2026SecureKey";

    // Extract email from JWT subject
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract userId
    public Long extractUserId(String token) {
        return extractAllClaims(token)
                .get("userId", Long.class);
    }

    // Extract role
    public String extractRole(String token) {
        return extractAllClaims(token)
                .get("role", String.class);
    }

    // Validate + parse JWT
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Same secret used in User/Product/Inventory/Cart/Order services
    private SecretKey getSignKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes()
        );
    }
}