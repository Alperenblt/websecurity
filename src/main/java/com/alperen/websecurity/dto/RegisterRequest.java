package com.alperen.websecurity.dto;

import com.alperen.websecurity.validation.PasswordComplex;
import com.alperen.websecurity.validation.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 80, message = "username must be 3-80 chars")
    @UniqueUsername
    private String username;

    @Email(message = "email must be valid")
    @Size(max = 100, message = "email max 100 chars")
    private String email;

    @NotBlank(message = "password is required")
    @PasswordComplex
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
