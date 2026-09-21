package com.smartfinance.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${finvisiq.jwt.secret}") String secret,
            @Value("${finvisiq.jwt.expiration-ms}") long expirationMs) {
        // Ensure secret has sufficient length (at least 256 bits / 32 bytes)
        String padded = secret;
        if (padded.getBytes(StandardCharsets.UTF_8).length < 32) {
            padded = String.format("%-32s", secret).replace(' ', '0');
        }
        this.key = Keys.hmacShaKeyFor(padded.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(int userId, String email, String name, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("name", name)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public UserPrincipal parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            int userId = Integer.parseInt(claims.getSubject());
            String email = claims.get("email", String.class);
            String name = claims.get("name", String.class);
            String role = claims.get("role", String.class);

            return new UserPrincipal(userId, email, name, role);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }
}
