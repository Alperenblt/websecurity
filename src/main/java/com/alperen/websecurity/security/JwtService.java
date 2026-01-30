package com.alperen.websecurity.security;

import com.alperen.websecurity.config.JwtProperties;
import com.alperen.websecurity.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_TYPE = "typ";

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        String secret = props.getSecret();
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("security.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getAccess().getExpirationSeconds());
        String role = normalizeRole(user.getRole());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getRefresh().getExpirationSeconds());
        String role = normalizeRole(user.getRole());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            Claims c = parse(token);
            Date exp = c.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    public String getRole(String token) {
        Object role = parse(token).get(CLAIM_ROLE);
        return normalizeRole(role == null ? "ROLE_USER" : role.toString());
    }

    public String getType(String token) {
        Object t = parse(token).get(CLAIM_TYPE);
        return t == null ? null : t.toString();
    }

    public Long getUserId(String token) {
        Object uid = parse(token).get(CLAIM_USER_ID);
        if (uid == null) return null;
        if (uid instanceof Number n) return n.longValue();
        return Long.parseLong(uid.toString());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) return "ROLE_USER";
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}

