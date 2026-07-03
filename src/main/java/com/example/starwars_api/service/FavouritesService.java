package com.example.starwars_api.service;

import com.example.starwars_api.dto.FavouriteDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavouritesService {

    private static final List<FavouriteDto> FAVOURITES = List.of(
            FavouriteDto.builder().id(1).name("Luke Skywalker").build(),
            FavouriteDto.builder().id(4).name("Darth Vader").build(),
            FavouriteDto.builder().id(10).name("Obi-Wan Kenobi").build()
    );

    public List<FavouriteDto> getFavourites() {
        return FAVOURITES;
    }
}
