package com.ospreys.cafeoj.controller;

import com.ospreys.cafeoj.model.User;
import com.ospreys.cafeoj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.registerUser(
                payload.get("username"),
                payload.get("name"),
                payload.get("email"),
                payload.get("password")
            );
            return ResponseEntity.ok(Map.of("message", "Registration successful", "username", user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        boolean authenticated = userService.authenticateUser(
            payload.get("username"),
            payload.get("password")
        );
        if (authenticated) {
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
    }
}
