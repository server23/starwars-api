package com.example.starwars_api.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogoutRequest {
    @JsonProperty("refreshToken")
    private String refreshToken;
}
