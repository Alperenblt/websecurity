package com.alperen.websecurity.controller;

import com.alperen.websecurity.config.JwtProperties;
import com.alperen.websecurity.dto.AccessTokenResponse;
import com.alperen.websecurity.dto.LoginRequest;
import com.alperen.websecurity.dto.LoginResponse;
import com.alperen.websecurity.dto.RegisterRequest;
import com.alperen.websecurity.model.User;
import com.alperen.websecurity.repository.UserRepository;
import com.alperen.websecurity.security.JwtService;
import com.alperen.websecurity.service.RefreshTokenService;
import com.alperen.websecurity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          UserService userService,
                          RefreshTokenService refreshTokenService,
                          JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest request) {
        userService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Login failed username={} reason={}", request.getUsername(), e.getMessage());
            throw e;
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String accessToken = jwtService.generateAccessToken(user);
        refreshTokenService.issueAndStore(user, response);
        addAccessCookie(response, accessToken);
        log.info("Login success username={}", user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request,
                                                       HttpServletResponse response) {
        String presented = refreshTokenService.readRefreshCookie(request).orElse(null);
        if (presented == null) {
            return ResponseEntity.status(401).build();
        }

        var result = refreshTokenService.rotate(presented, response);
        if (result.status() != RefreshTokenService.RotationResult.Status.OK) {
            clearAccessCookie(response); // Ensure access cookie is also cleared if refresh fails
            return ResponseEntity.status(401).build();
        }

        String accessToken = jwtService.generateAccessToken(result.user());
        addAccessCookie(response, accessToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenService.revokeIfPresent(request, response);
        clearAccessCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void addAccessCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getAccess().getCookieName(), token)
                .httpOnly(true)
                .secure(jwtProperties.getRefresh().getCookie().isSecure()) // Reuse secure setting or add specific one
                .path("/")
                .sameSite(jwtProperties.getRefresh().getCookie().getSameSite()) // Reuse SameSite or add specific one
                .maxAge(jwtProperties.getAccess().getExpirationSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAccessCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getAccess().getCookieName(), "")
                .httpOnly(true)
                .secure(jwtProperties.getRefresh().getCookie().isSecure())
                .path("/")
                .sameSite(jwtProperties.getRefresh().getCookie().getSameSite())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}