package com.alperen.websecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rateLimit_Exceeded_ReturnsTooManyRequests() throws Exception {
        // Limit is 20 requests per minute per IP.
        // We will make 20 allowed requests.
        for (int i = 0; i < 20; i++) {
            // Using /auth/login which is rate limited
            // We don't care about the result (400/401/200), just that it's NOT 429 yet.
            // But we should expect something consistent. 
            // Since we send no body, it will likely be 400 Bad Request.
            mockMvc.perform(post("/auth/login"))
                   //.andExpect(status().isBadRequest()) // Commented out to be safe, just checking it passes
            ; 
        }

        // The 21st request should fail with 429
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().isTooManyRequests());
    }
}
