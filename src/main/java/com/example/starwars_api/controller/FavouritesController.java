package com.example.starwars_api.controller;

import com.example.starwars_api.dto.FavouriteDto;
import com.example.starwars_api.exception.UnauthorizedException;
import com.example.starwars_api.service.FavouritesService;
import com.example.starwars_api.service.TokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FavouritesController {

    private final TokenService tokenService;
    private final FavouritesService favouritesService;

    public FavouritesController(TokenService tokenService, FavouritesService favouritesService) {
        this.tokenService = tokenService;
        this.favouritesService = favouritesService;
    }

    @GetMapping("/favourites")
    public List<FavouriteDto> getFavourites(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String accessToken = extractAccessToken(authHeader);
        tokenService.validateAccessToken(accessToken);
        return favouritesService.getFavourites();
    }

    private String extractAccessToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid authorization header");
        }
        return authHeader.substring(7);
    }
}
