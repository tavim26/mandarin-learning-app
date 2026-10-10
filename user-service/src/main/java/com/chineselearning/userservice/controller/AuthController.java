package com.chineselearning.userservice.controller;

import com.chineselearning.userservice.domain.dto.AuthRequestDto;
import com.chineselearning.userservice.domain.dto.AuthResponseDto;
import com.chineselearning.userservice.domain.dto.RegisterRequestDto;
import com.chineselearning.userservice.domain.dto.RegisterResponseDto;
import com.chineselearning.userservice.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController
{

    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new STUDENT or TEACHER account and returns user info")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request, BindingResult bindingResult)
    {
        if (bindingResult.hasErrors())
        {
            return ResponseEntity.badRequest().body(errorBody(bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }

        try {
            RegisterResponseDto response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user credentials and returns JWT token")
    public ResponseEntity<?> login(@RequestBody AuthRequestDto request)
    {
        try {
            AuthResponseDto response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody("Account is banned"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody("Invalid credentials"));
        }
    }

    private static Map<String, String> errorBody(String message)
    {
        return Map.of("message", message);
    }
}