package com.example.smarthub.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类 — 负责令牌的生成、解析、验证
 * 使用 HMAC-SHA256 签名算法
 */
@Slf4j
@Component
public class JwtUtil {

    /** HMAC-SHA256 密钥 */
    private final SecretKey key;
    /** Access Token 过期时间（毫秒） */
    private final long expiration;
    /** Refresh Token 过期时间（毫秒） */
    private final long refreshExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * 生成 Access Token（使用默认过期时间）
     * @param userId    用户ID
     * @param username  用户名
     * @param claims    额外自定义声明
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username, Map<String, Object> claims) {
        return generateTokenWithTtl(userId, username, claims, expiration / 1000);
    }

    /**
     * 生成 Access Token（自定义过期时间，单位：小时）
     * @param userId    用户ID
     * @param username  用户名
     * @param claims    额外自定义声明
     * @param ttlHours  过期时间（小时），默认 2
     * @return JWT 字符串
     */
    public String generateTokenWithTtl(Long userId, String username, Map<String, Object> claims, long ttlHours) {
        long ttlMillis = ttlHours * 3600_000L;
        Map<String, Object> allClaims = new HashMap<>();
        allClaims.putAll(claims);
        allClaims.put("userId", userId);
        allClaims.put("username", username);
        return Jwts.builder()
                .claims(allClaims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 生成 Refresh Token（仅包含 username）
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * 解析并返回 Claims
     * @throws Exception Token 无效时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 Token 中提取用户ID */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /** 从 Token 中提取用户名 */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 检查 Token 是否已过期
     * @return true=已过期, false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证 Token 合法性（未过期且签名正确）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
