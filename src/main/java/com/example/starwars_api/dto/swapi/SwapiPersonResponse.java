package com.example.starwars_api.dto.swapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class SwapiPersonResponse {
    private String name;
    private String height;
    private String mass;

    @JsonProperty("birth_year")
    private String birthYear;

    private List<String> films;
    private String created;
    private String url;
}
