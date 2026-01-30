package com.alperen.websecurity.service;

import com.alperen.websecurity.model.User;
import com.alperen.websecurity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        // Arrange
        String username = "validUser";
        String email = "valid@example.com";
        String password = "StrongPassword123!";
        String encodedPass = "encodedHash";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPass);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(username, email, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(encodedPass, result.getPassword()); // Should store encoded password
        assertEquals("ROLE_USER", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("duplicate")).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser("duplicate", "email@test.com", "pass")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShortUsername() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser("ab", "email@test.com", "pass")
        );
    }

    @Test
    void createUser_InvalidCharacters() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser("user@name", "email@test.com", "pass")
        );
    }
}
