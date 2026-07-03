package com.example.starwars_api.dto.swapi;

import lombok.Data;
import java.util.List;

@Data
public class SwapiPeoplePageResponse {
    private int count;
    private String next;
    private String previous;
    private List<SwapiPersonResponse> results;
}
