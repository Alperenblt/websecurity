package com.alperen.websecurity.config;

import com.alperen.websecurity.filter.CsrfCookieFilter;
import com.alperen.websecurity.security.JwtAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .headers(headers -> headers
                        .contentTypeOptions(c -> {})
                        .frameOptions(f -> f.deny())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'")
                        )
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Public access to clean URLs and static resources
                        .requestMatchers("/", "/login", "/register", "/dashboard").permitAll()
                        .requestMatchers("/index.html", "/login.html", "/register.html", "/dashboard.html").permitAll()
                        .requestMatchers("/js/**", "/css/**", "/static/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loggingAuthEntryPoint())
                        .accessDeniedHandler(loggingAccessDeniedHandler())
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint loggingAuthEntryPoint() {
        return (request, response, authException) -> {
            log.warn("Unauthorized access path={} method={} remote={}",
                    request.getRequestURI(), request.getMethod(), request.getRemoteAddr());
            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED).commence(request, response, authException);
        };
    }

    private AccessDeniedHandler loggingAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("Forbidden access path={} method={} remote={}",
                    request.getRequestURI(), request.getMethod(), request.getRemoteAddr());
            response.setStatus(HttpStatus.FORBIDDEN.value());
        };
    }
}