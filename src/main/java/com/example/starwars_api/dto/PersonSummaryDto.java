package com.example.starwars_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonSummaryDto {
    private int id;
    private String name;
}
