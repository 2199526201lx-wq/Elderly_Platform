package com.example.elderly_Platform.core.util;

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

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire}")
    private long accessExpire;

    @Value("${jwt.refresh-token-expire}")
    private long refreshExpire;

    public String generateAccessToken(Long userId, String role) {
        return buildToken(userId, role, accessExpire * 1000);
    }

    public String generateRefreshToken(Long userId, String role) {
        return buildToken(userId, role, refreshExpire * 1000);
    }

    private String buildToken(Long userId, String role, long expireMs) {
        SecretKey key =
                Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token){

        return Long.valueOf(parse(token).getSubject());
    }

    public String getRole(String token){
        return parse(token).get("role",String.class);
    }

    public Date getIssuedAt(String token) {
        return parse(token).getIssuedAt();
    }

    private Claims parse(String token){
        SecretKey key=
                Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
