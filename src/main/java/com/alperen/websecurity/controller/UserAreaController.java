package com.alperen.websecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAreaController {

    @GetMapping("/user/home")
    public String userHome() {
        return "USER area";
    }
}