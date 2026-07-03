package com.example.starwars_api.client;

import com.example.starwars_api.dto.swapi.SwapiPeoplePageResponse;
import com.example.starwars_api.dto.swapi.SwapiPersonResponse;
import com.example.starwars_api.exception.PersonNotFoundException;
import com.example.starwars_api.exception.SwapiUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class SwapiClient {

    private static final String BASE_URL = "https://swapi.dev/api";
    private final RestClient restClient;

    public SwapiClient() {
        this.restClient = RestClient.create(BASE_URL);
    }

    public SwapiPeoplePageResponse getPeoplePage(int page) {
        log.info("Fetching SWAPI people page {}", page);
        try {
            return restClient.get()
                    .uri("/people/?page={page}", page)
                    .retrieve()
                    .body(SwapiPeoplePageResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("SWAPI page not found: {}", page);
            throw new PersonNotFoundException("Page not found: " + page);
        } catch (Exception e) {
            log.error("SWAPI request failed for people page {}", page, e);
            throw new SwapiUnavailableException("SWAPI is unavailable");
        }
    }

    public SwapiPersonResponse getPersonById(int id) {
        log.info("Fetching SWAPI person {}", id);
        try {
            return restClient.get()
                    .uri("/people/{id}/", id)
                    .retrieve()
                    .body(SwapiPersonResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("SWAPI person not found: {}", id);
            throw new PersonNotFoundException("Person not found: " + id);
        } catch (Exception e) {
            log.error("SWAPI request failed for person {}", id, e);
            throw new SwapiUnavailableException("SWAPI is unavailable");
        }
    }
}
