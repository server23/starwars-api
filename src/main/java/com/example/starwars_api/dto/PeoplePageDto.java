package com.example.starwars_api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PeoplePageDto {
    private int count;
    private String next;
    private String previous;
    private List<PersonSummaryDto> results;
}
