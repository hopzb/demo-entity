package com.taxi.demo.controller;

import com.taxi.demo.dto.RegisterUserRequest;
import com.taxi.demo.dto.UserResponseDTO;
import com.taxi.demo.dto.auth.LoginRequest;
import com.taxi.demo.dto.auth.LoginResponse;
import com.taxi.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDTO register(@RequestBody RegisterUserRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public UserResponseDTO me(Authentication authentication) {
        return userService.getMe(authentication);
    }
}
