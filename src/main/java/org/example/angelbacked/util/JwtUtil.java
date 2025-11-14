package org.example.angelbacked.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 设置过期时间 24 小时
    public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60;

    // 固定密钥，确保每次重启应用后仍能验证之前的token
    private final SecretKey secretKey = Keys.hmacShaKeyFor("ThisIsASecretKeyThatIsAtLeast512BitsLongAndIsUsedForGeneratingSecureTokensInOurApplication".getBytes());

    // 从 token 中获取用户名
    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 从 token 中获取过期时间
    public Date getExpirationDateFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        final Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claimsResolver.apply(claims);
    }

    // 解析 token 获取所有声明
    private Claims getAllClaimsFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            // 如果token格式不正确，则返回null
            return null;
        }
    }

    // 检查 token 是否过期
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return true; // 如果无法获取过期时间，认为token已过期
        }
        return expiration.before(new Date());
    }

    // 生成 token
    public String generateToken(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username);
    }

    // 创建 token
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                    .signWith(secretKey)
                    .compact();
        } catch (Exception e) {
            System.err.println("Failed to generate JWT token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 验证 token
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = getUsernameFromToken(token);
        if (extractedUsername == null) {
            return false; // 如果无法提取用户名，则认为token无效
        }
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}