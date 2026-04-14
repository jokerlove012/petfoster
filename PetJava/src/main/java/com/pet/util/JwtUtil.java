package com.pet.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌，实现用户身份认证
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥，从配置文件读取
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（毫秒），从配置文件读取
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param role 用户角色
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return createToken(claims, userId);
    }

    /**
     * 创建JWT令牌的内部方法
     * @param claims 令牌中包含的自定义声明
     * @param subject 令牌主题（通常是用户ID）
     * @return 生成的JWT令牌字符串
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param token JWT令牌字符串
     * @return 令牌中的声明对象
     * @throws ExpiredJwtException 令牌过期异常
     * @throws JwtException 令牌解析异常
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从令牌中获取用户ID
     * @param token JWT令牌字符串
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("userId");
    }

    /**
     * 从令牌中获取用户角色
     * @param token JWT令牌字符串
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("role");
    }

    /**
     * 检查令牌是否已过期
     * @param token JWT令牌字符串
     * @return true表示已过期，false表示未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证令牌是否有效
     * @param token JWT令牌字符串
     * @return true表示有效，false表示无效
     */
    public Boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
