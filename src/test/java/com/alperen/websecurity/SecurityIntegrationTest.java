package com.alperen.websecurity;

import com.alperen.websecurity.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.alperen.websecurity.repository.UserRepository userRepository;

    @BeforeEach
    void setupUser() {
        if (userRepository.findByUsername("user").isEmpty()) {
            com.alperen.websecurity.model.User user = new com.alperen.websecurity.model.User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password"));
            user.setEmail("user@example.com");
            user.setRole("USER");
            userRepository.save(user);
        }
    }

    @Test
    void accessSecuredEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAndAccessSecuredEndpoint_Success() throws Exception {
        // 1. Login to get cookies
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        
        // 2. Access secured endpoint with cookies
        mockMvc.perform(get("/api/notes")
                        .cookie(cookies))
                .andExpect(status().isOk());
    }

    @Test
    void postRequest_WithoutCsrfToken_ReturnsForbidden() throws Exception {
        // 1. Login to get auth cookie
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        Cookie[] cookies = loginResult.getResponse().getCookies();

        // 2. Try to create a note WITHOUT CSRF token
        mockMvc.perform(post("/api/notes")
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"content\":\"Content\"}"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void postRequest_WithCsrfToken_Success() throws Exception {
        // 1. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie[] authCookies = loginResult.getResponse().getCookies();
        
        // 2. Create note WITH CSRF token
        mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .cookie(authCookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"content\":\"Content\"}"))
                .andExpect(status().isOk());
    }
}
