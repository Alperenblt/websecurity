package com.alperen.websecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminAreaController {

    @GetMapping("/admin/home")
    public String adminHome() {
        return "ADMIN area";
    }
}