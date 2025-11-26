package com.ospreys.cafeoj.controller;

import com.ospreys.cafeoj.model.User;
import com.ospreys.cafeoj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            userService.findByUsername(username).ifPresent(user -> 
                model.addAttribute("user", user)
            );
        }
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String username = payload.get("username");
        String password = payload.get("password");

        User user = userService.authenticate(username, password);
        
        if (user != null) {
            // Manually set authentication in Security Context
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user.getUsername(), 
                    null, 
                    java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole()))
                );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            securityContextRepository.saveContext(org.springframework.security.core.context.SecurityContextHolder.getContext(), request, null);
            
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
    }
}
