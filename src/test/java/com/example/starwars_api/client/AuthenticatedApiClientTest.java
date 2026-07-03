package com.example.starwars_api.client;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthenticatedApiClientTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getFavouritesRefreshesAndRetriesOnUnauthorized() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(loginResponse, "$.accessToken");
        String refreshToken = JsonPath.read(loginResponse, "$.refreshToken");

        AuthenticatedApiClient client = new AuthenticatedApiClient("http://localhost:" + port);
        setToken(client, "accessToken", accessToken);
        setToken(client, "refreshToken", refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        var favourites = client.getFavourites();

        assertThat(favourites).hasSize(3);
        assertThat(favourites.get(0).getName()).isEqualTo("Luke Skywalker");
    }

    private void setToken(AuthenticatedApiClient client, String fieldName, String value) throws Exception {
        Field field = AuthenticatedApiClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(client, value);
    }
}
