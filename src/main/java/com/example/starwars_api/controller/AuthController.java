package com.example.starwars_api.controller;

import com.example.starwars_api.dto.auth.*;
import com.example.starwars_api.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return tokenService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest request) {
        String accessToken = tokenService.refresh(request.getRefreshToken());
        return RefreshResponse.builder().accessToken(accessToken).build();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody LogoutRequest request) {
        tokenService.logout(request.getRefreshToken());
    }
}
