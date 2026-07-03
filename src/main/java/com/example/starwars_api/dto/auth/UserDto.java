package com.example.starwars_api.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private int id;
    private String username;
}
