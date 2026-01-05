package com.alperen.websecurity.service;

import com.alperen.websecurity.model.User;
import com.alperen.websecurity.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        return userRepository.save(new User(username, email, password));
    }

    public boolean authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .map(u -> u.getPassword().equals(password))
                .orElse(false);
    }
}