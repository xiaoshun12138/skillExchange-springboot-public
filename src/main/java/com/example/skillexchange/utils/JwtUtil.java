package com.example.skillexchange.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT令牌工具类
 * 适配JJWT 0.12.6 + Spring Boot 3.3.5
 * 
 * 功能说明：
 * 1. 生成token：用户登录成功后，生成JWT令牌返回给前端
 * 2. 校验token：拦截器调用此方法校验用户登录状态
 * 3. 从token获取openid：通过token识别用户身份
 * 
 * 本项目仅为信息公告板，token仅用于识别用户身份，不做任何社交相关功能
 */
@Component
public class JwtUtil {

    /** JWT密钥，从application.yml读取 */
    @Value("${jwt.secret}")
    private String secret;

    /** token过期时间（毫秒），从application.yml读取 */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成密钥对象
     * JJWT 0.12.6要求密钥至少256位（32字节）
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * @param openid 微信用户唯一标识
     * @param userId 用户ID
     * @return JWT令牌字符串
     */
    public String generateToken(String openid, Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(openid)                           // 主题：存放openid
                .claim("userId", userId)                   // 自定义声明：存放用户ID
                .issuedAt(now)                             // 签发时间
                .expiration(expireDate)                    // 过期时间
                .signWith(getSigningKey())                 // 使用密钥签名
                .compact();
    }

    /**
     * 校验token是否有效
     * @param token JWT令牌
     * @return true=有效，false=无效或过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取用户openid
     * @param token JWT令牌
     * @return 微信用户openid
     */
    public String getOpenidFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从token中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 解析token，返回Claims对象
     * @param token JWT令牌
     * @return Claims对象（包含token中的所有声明信息）
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())               // 验证签名
                .build()
                .parseSignedClaims(token)                  // 解析已签名的Claims
                .getPayload();
    }
}
