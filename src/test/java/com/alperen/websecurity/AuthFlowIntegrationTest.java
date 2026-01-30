package com.alperen.websecurity;

import com.alperen.websecurity.dto.LoginRequest;
import com.alperen.websecurity.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM refresh_tokens");
        jdbcTemplate.update("DELETE FROM notes");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void login_refresh_rotation_and_protected_route() throws Exception {
        // register
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("alice");
        reg.setEmail("alice@example.com");
        reg.setPassword("Abcdef1!");

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // login (expect refresh cookie + access token cookie)
        LoginRequest login = new LoginRequest();
        login.setUsername("alice");
        login.setPassword("Abcdef1!");

        MvcResult loginRes = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        List<String> cookies = loginRes.getResponse().getHeaders("Set-Cookie");
        assertThat(cookies).hasSizeGreaterThanOrEqualTo(2);

        String refreshCookieVal = extractCookieValue(cookies, "refresh_token");
        String accessCookieVal = extractCookieValue(cookies, "access_token");

        assertThat(refreshCookieVal).isNotBlank();
        assertThat(accessCookieVal).isNotBlank();

        // access protected route using COOKIE (not header)
        mockMvc.perform(get("/api/notes")
                        .cookie(new Cookie("access_token", accessCookieVal)))
                .andExpect(status().isOk());

        // refresh: rotate cookie + new access token
        MvcResult refresh1 = mockMvc.perform(post("/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", refreshCookieVal)))
                .andExpect(status().isOk())
                .andReturn();

        List<String> refreshCookies = refresh1.getResponse().getHeaders("Set-Cookie");
        String refresh2Value = extractCookieValue(refreshCookies, "refresh_token");
        String access2Value = extractCookieValue(refreshCookies, "access_token");

        assertThat(refresh2Value).isNotEqualTo(refreshCookieVal);
        assertThat(access2Value).isNotEqualTo(accessCookieVal);

        // using the old refresh cookie again should be rejected (rotation)
        mockMvc.perform(post("/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", refreshCookieVal)))
                .andExpect(status().isUnauthorized());

        // logout: clear refresh cookie and access cookie
        mockMvc.perform(post("/auth/logout")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", refresh2Value)))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    void data_isolation_userA_cannot_access_userB_note() throws Exception {
        RegisterRequest regA = new RegisterRequest();
        regA.setUsername("userA");
        regA.setEmail("a@example.com");
        regA.setPassword("Abcdef1!");
        
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regA)))
                .andExpect(status().isOk());

        RegisterRequest regB = new RegisterRequest();
        regB.setUsername("userB");
        regB.setEmail("b@example.com");
        regB.setPassword("Abcdef1!");
        
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regB)))
                .andExpect(status().isOk());

        LoginRequest loginA = new LoginRequest();
        loginA.setUsername("userA");
        loginA.setPassword("Abcdef1!");
        
        MvcResult loginARes = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn();
        List<String> cookiesA = loginARes.getResponse().getHeaders("Set-Cookie");
        String accessCookieA = extractCookieValue(cookiesA, "access_token");

        // userA creates a note
        String noteJson = "{\"title\":\"SecretA\",\"content\":\"Only A\"}";
        MvcResult createRes = mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .cookie(new Cookie("access_token", accessCookieA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteJson))
                .andExpect(status().isOk())
                .andReturn();
        String body = createRes.getResponse().getContentAsString();
        long noteId = extractId(body);

        // login as userB
        LoginRequest loginB = new LoginRequest();
        loginB.setUsername("userB");
        loginB.setPassword("Abcdef1!");
        
        MvcResult loginBRes = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn();
        List<String> cookiesB = loginBRes.getResponse().getHeaders("Set-Cookie");
        String accessCookieB = extractCookieValue(cookiesB, "access_token");

        // userB tries to access userA's note -> expect 404 (data isolation)
        mockMvc.perform(get("/api/notes/" + noteId)
                        .cookie(new Cookie("access_token", accessCookieB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_DuplicateUsername_ReturnsBadRequest() throws Exception {
        RegisterRequest reg1 = new RegisterRequest();
        reg1.setUsername("duplicate");
        reg1.setEmail("d1@example.com");
        reg1.setPassword("Abcdef1!");

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg1)))
                .andExpect(status().isOk());

        RegisterRequest reg2 = new RegisterRequest();
        reg2.setUsername("duplicate");
        reg2.setEmail("d2@example.com");
        reg2.setPassword("Abcdef1!");

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Username is already taken"));
    }

    private static String extractCookieValue(List<String> headers, String cookieName) {
        for (String header : headers) {
            if (header.startsWith(cookieName + "=")) {
                int start = cookieName.length() + 1;
                int end = header.indexOf(';', start);
                if (end < 0) end = header.length();
                return header.substring(start, end);
            }
        }
        throw new IllegalArgumentException("Cookie not found: " + cookieName);
    }

    private static long extractId(String json) {
        int idx = json.indexOf("\"id\":");
        if (idx < 0) throw new IllegalArgumentException("id not found");
        int start = idx + 5;
        int end = json.indexOf(',', start);
        if (end < 0) end = json.indexOf('}', start);
        return Long.parseLong(json.substring(start, end).trim());
    }
}
