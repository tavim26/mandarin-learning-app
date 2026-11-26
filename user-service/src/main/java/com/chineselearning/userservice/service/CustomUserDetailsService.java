package com.chineselearning.userservice.service;

import com.chineselearning.userservice.entity.CredentialEntity;
import com.chineselearning.userservice.repository.CredentialRepository;
import com.chineselearning.userservice.util.Emails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
    private final CredentialRepository credentialRepository;

    public CustomUserDetailsService(CredentialRepository credentialRepository)
    {
        this.credentialRepository = credentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        CredentialEntity credential = credentialRepository.findByEmail(Emails.normalize(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Bans are checked by AuthService after the password, so they are not revealed to strangers.
        return User.withUsername(credential.getEmail())
                .password(credential.getPasswordHash())
                .roles(credential.getRole().name())
                .build();
    }
}