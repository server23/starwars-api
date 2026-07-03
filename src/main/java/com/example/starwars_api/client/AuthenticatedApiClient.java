package com.example.starwars_api.client;

import com.example.starwars_api.dto.FavouriteDto;
import com.example.starwars_api.dto.auth.LoginRequest;
import com.example.starwars_api.dto.auth.LoginResponse;
import com.example.starwars_api.dto.auth.RefreshRequest;
import com.example.starwars_api.dto.auth.RefreshResponse;
import com.example.starwars_api.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Supplier;

@Component
public class AuthenticatedApiClient {

    private final RestClient restClient;
    private String accessToken;
    private String refreshToken;

    public AuthenticatedApiClient(@Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public void login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        LoginResponse response = restClient.post()
                .uri("/auth/login")
                .body(request)
                .retrieve()
                .body(LoginResponse.class);

        if (response == null) {
            throw new UnauthorizedException("Login failed");
        }

        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();
    }

    public List<FavouriteDto> getFavourites() {
        return executeWithRefresh(() -> restClient.get()
                .uri("/favourites")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<FavouriteDto>>() {}));
    }

    private <T> T executeWithRefresh(Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException.Unauthorized e) {
            refreshAccessToken();
            try {
                return request.get();
            } catch (HttpClientErrorException.Unauthorized retryException) {
                throw new UnauthorizedException("Authentication failed after refresh");
            }
        }
    }

    private void refreshAccessToken() {
        try {
            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken(refreshToken);

            RefreshResponse response = restClient.post()
                    .uri("/auth/refresh")
                    .body(request)
                    .retrieve()
                    .body(RefreshResponse.class);

            if (response == null || response.getAccessToken() == null) {
                throw new UnauthorizedException("Refresh failed");
            }

            this.accessToken = response.getAccessToken();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UnauthorizedException("Refresh failed");
        }
    }
}
