package com.chineselearning.userservice.controller;

import com.chineselearning.userservice.dto.AuthResponseDto;
import com.chineselearning.userservice.dto.LoginRequestDto;
import com.chineselearning.userservice.dto.RegisterResponseDto;
import com.chineselearning.userservice.dto.StudentRegisterRequestDto;
import com.chineselearning.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new student account")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody StudentRegisterRequestDto request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request)
    {
        return authService.login(request);
    }
}