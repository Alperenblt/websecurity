package com.alperen.websecurity.service;

import com.alperen.websecurity.model.User;
import com.alperen.websecurity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String email, String rawPassword) {
        if (username == null || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("Username must contain only alphanumeric characters");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }
}