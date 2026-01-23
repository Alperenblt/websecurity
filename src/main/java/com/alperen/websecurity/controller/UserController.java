package com.alperen.websecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String user() {
        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return "Hello USER: " + email;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return "Hello ADMIN: " + email;
    }
}