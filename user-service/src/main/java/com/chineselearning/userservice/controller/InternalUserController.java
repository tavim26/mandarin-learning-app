package com.chineselearning.userservice.controller;

import com.chineselearning.userservice.dto.TokenStatusDto;
import com.chineselearning.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints. The gateway does not route /internal/**, so these are
 * reachable only from inside the Docker network.
 */
@Hidden
@RestController
@RequestMapping("/internal/users")
public class InternalUserController
{
    private final UserService userService;

    public InternalUserController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping("/{id}/token-status")
    public TokenStatusDto getTokenStatus(@PathVariable Long id)
    {
        return userService.getTokenStatus(id);
    }
}