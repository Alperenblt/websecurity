package com.alperen.websecurity.service;

import com.alperen.websecurity.config.JwtProperties;
import com.alperen.websecurity.model.RefreshToken;
import com.alperen.websecurity.model.User;
import com.alperen.websecurity.repository.RefreshTokenRepository;
import com.alperen.websecurity.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JwtService jwtService,
                               JwtProperties jwtProperties,
                               RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void setRefreshCookie(HttpServletResponse response, String refreshTokenJwt) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefresh().getCookieName(), refreshTokenJwt)
                .httpOnly(true)
                .secure(jwtProperties.getRefresh().getCookie().isSecure())
                .path("/auth")
                .sameSite(jwtProperties.getRefresh().getCookie().getSameSite())
                .maxAge(jwtProperties.getRefresh().getExpirationSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefresh().getCookieName(), "")
                .httpOnly(true)
                .secure(jwtProperties.getRefresh().getCookie().isSecure())
                .path("/auth")
                .sameSite(jwtProperties.getRefresh().getCookie().getSameSite())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public Optional<String> readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        String name = jwtProperties.getRefresh().getCookieName();
        for (var c : request.getCookies()) {
            if (name.equals(c.getName())) {
                String v = c.getValue();
                return (v == null || v.isBlank()) ? Optional.empty() : Optional.of(v);
            }
        }
        return Optional.empty();
    }

    @Transactional
    public String issueAndStore(User user, HttpServletResponse response) {
        String refreshJwt = jwtService.generateRefreshToken(user);
        RefreshToken row = new RefreshToken();
        row.setUser(user);
        row.setTokenHash(hash(refreshJwt));
        row.setExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefresh().getExpirationSeconds()));
        refreshTokenRepository.save(row);
        setRefreshCookie(response, refreshJwt);
        return refreshJwt;
    }

    @Transactional
    public void revokeIfPresent(HttpServletRequest request, HttpServletResponse response) {
        readRefreshCookie(request).ifPresent(raw -> {
            String hash = hash(raw);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
                if (rt.getRevokedAt() == null) {
                    rt.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(rt);
                }
            });
        });
        clearRefreshCookie(response);
    }

    /**
     * Refresh token rotation:
     * - if token is valid and active: revoke it, mint/store a new one, set cookie, return new access token.
     * - if token is reused (already revoked): revoke all active tokens for user (possible theft), clear cookie.
     */
    @Transactional
    public RotationResult rotate(String presentedRefreshJwt, HttpServletResponse response) {
        if (!jwtService.isValid(presentedRefreshJwt) || !"refresh".equals(jwtService.getType(presentedRefreshJwt))) {
            clearRefreshCookie(response);
            return RotationResult.invalid();
        }

        String tokenHash = hash(presentedRefreshJwt);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);
        if (existing == null) {
            clearRefreshCookie(response);
            return RotationResult.invalid();
        }

        Long userId = existing.getUser().getId();
        Instant now = Instant.now();

        if (existing.getRevokedAt() != null) {
            // token reuse -> defensive revoke all
            refreshTokenRepository.revokeAllActiveForUser(userId, now);
            clearRefreshCookie(response);
            log.warn("Refresh token reuse detected for userId={}", userId);
            return RotationResult.reused();
        }

        if (existing.getExpiresAt() != null && existing.getExpiresAt().isBefore(now)) {
            existing.setRevokedAt(now);
            refreshTokenRepository.save(existing);
            clearRefreshCookie(response);
            return RotationResult.invalid();
        }

        User user = existing.getUser();
        String newRefreshJwt = jwtService.generateRefreshToken(user);

        RefreshToken replacement = new RefreshToken();
        replacement.setUser(user);
        replacement.setTokenHash(hash(newRefreshJwt));
        replacement.setExpiresAt(now.plusSeconds(jwtProperties.getRefresh().getExpirationSeconds()));
        refreshTokenRepository.save(replacement);

        existing.setRevokedAt(now);
        existing.setReplacedBy(replacement);
        refreshTokenRepository.save(existing);

        setRefreshCookie(response, newRefreshJwt);
        return RotationResult.ok(user);
    }

    private String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String pepper = jwtProperties.getRefresh().getHashPepper();
            if (pepper == null) pepper = "";
            byte[] bytes = md.digest((token + pepper).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash refresh token", e);
        }
    }

    public record RotationResult(Status status, User user) {
        public enum Status { OK, INVALID, REUSED }
        public static RotationResult ok(User user) { return new RotationResult(Status.OK, user); }
        public static RotationResult invalid() { return new RotationResult(Status.INVALID, null); }
        public static RotationResult reused() { return new RotationResult(Status.REUSED, null); }
    }
}
