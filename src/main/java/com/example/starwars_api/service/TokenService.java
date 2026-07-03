package com.example.starwars_api.service;

import com.example.starwars_api.dto.auth.LoginResponse;
import com.example.starwars_api.dto.auth.UserDto;
import com.example.starwars_api.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private static final String VALID_USERNAME = "user";
    private static final String VALID_PASSWORD = "password";

    private final Map<String, UserDto> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, UserDto> refreshTokens = new ConcurrentHashMap<>();

    public LoginResponse login(String username, String password) {
        if (!VALID_USERNAME.equals(username) || !VALID_PASSWORD.equals(password)) {
            throw new UnauthorizedException("Invalid credentials");
        }

        UserDto user = UserDto.builder().id(1).username(username).build();
        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        accessTokens.put(accessToken, user);
        refreshTokens.put(refreshToken, user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }

    public String refresh(String refreshToken) {
        UserDto user = refreshTokens.get(refreshToken);
        if (user == null) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String newAccessToken = UUID.randomUUID().toString();
        accessTokens.entrySet().removeIf(entry -> entry.getValue().equals(user));
        accessTokens.put(newAccessToken, user);
        return newAccessToken;
    }

    public void logout(String refreshToken) {
        UserDto user = refreshTokens.remove(refreshToken);
        if (user != null) {
            accessTokens.entrySet().removeIf(entry -> entry.getValue().equals(user));
        }
    }

    public UserDto validateAccessToken(String accessToken) {
        UserDto user = accessTokens.get(accessToken);
        if (user == null) {
            throw new UnauthorizedException("Invalid or expired access token");
        }
        return user;
    }
}
