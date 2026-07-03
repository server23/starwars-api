package com.example.starwars_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonDetailDto {
    private String name;
    private Double height;          // metri

    @JsonProperty("birth_year")
    private String birthYear;

    private Double mass;            // kg

    @JsonProperty("number_of_films")
    private int numberOfFilms;

    @JsonProperty("date_added")
    private String dateAdded;       // dd-MM-yyyy
}
