package com.chineselearning.userservice.service;

import com.chineselearning.userservice.dto.AuthResponseDto;
import com.chineselearning.userservice.dto.LoginRequestDto;
import com.chineselearning.userservice.dto.RegisterResponseDto;
import com.chineselearning.userservice.dto.StudentRegisterRequestDto;
import com.chineselearning.userservice.entity.CredentialEntity;
import com.chineselearning.userservice.entity.Role;
import com.chineselearning.userservice.repository.CredentialRepository;
import com.chineselearning.userservice.util.Emails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService
{
    private final AuthenticationManager authenticationManager;
    private final CredentialRepository credentialRepository;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, CredentialRepository credentialRepository, UserService userService, JwtService jwtService)
    {
        this.authenticationManager = authenticationManager;
        this.credentialRepository = credentialRepository;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponseDto register(StudentRegisterRequestDto request)
    {
        CredentialEntity credential = userService.createAccount(request.getEmail(), request.getPassword(), request.getFullName(), Role.STUDENT);

        return new RegisterResponseDto(credential.getId(), credential.getRole(), credential.getUser().getFullName());
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request)
    {
        String email = Emails.normalize(request.getEmail());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        CredentialEntity credential = credentialRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!credential.isActive())
        {
            throw new DisabledException("Account is banned");
        }

        return new AuthResponseDto(
                jwtService.generateToken(credential),
                credential.getId(),
                credential.getRole(),
                credential.getUser().getFullName()
        );
    }
}