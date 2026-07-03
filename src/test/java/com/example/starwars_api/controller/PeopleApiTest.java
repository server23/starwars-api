package com.example.starwars_api.controller;

import com.example.starwars_api.client.SwapiClient;
import com.example.starwars_api.dto.swapi.SwapiPeoplePageResponse;
import com.example.starwars_api.dto.swapi.SwapiPersonResponse;
import com.example.starwars_api.exception.PersonNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PeopleApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwapiClient swapiClient;

    @Test
    void getPeopleReturnsPaginatedList() throws Exception {
        SwapiPersonResponse luke = person("Luke Skywalker", "https://swapi.dev/api/people/1/");
        SwapiPeoplePageResponse page = pageResponse(82, "https://swapi.dev/api/people/?page=2", null, List.of(luke));

        when(swapiClient.getPeoplePage(1)).thenReturn(page);

        mockMvc.perform(get("/people").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(82))
                .andExpect(jsonPath("$.results[0].id").value(1))
                .andExpect(jsonPath("$.results[0].name").value("Luke Skywalker"));
    }

    @Test
    void getPeopleDefaultsToPageOne() throws Exception {
        SwapiPeoplePageResponse page = pageResponse(82, null, null, List.of());
        when(swapiClient.getPeoplePage(1)).thenReturn(page);

        mockMvc.perform(get("/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(82));
    }

    @Test
    void getPeopleRejectsInvalidPage() throws Exception {
        mockMvc.perform(get("/people").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page must be >= 1"));

        mockMvc.perform(get("/people").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page must be >= 1"));
    }

    @Test
    void getPeopleCachesSwapiPages() throws Exception {
        SwapiPeoplePageResponse page = pageResponse(82, null, null, List.of());
        when(swapiClient.getPeoplePage(1)).thenReturn(page);

        mockMvc.perform(get("/people").param("page", "1")).andExpect(status().isOk());
        mockMvc.perform(get("/people").param("page", "1")).andExpect(status().isOk());

        verify(swapiClient, times(1)).getPeoplePage(1);
    }

    @Test
    void getPersonByIdReturnsFormattedDetails() throws Exception {
        SwapiPersonResponse luke = fullPerson(
                "Luke Skywalker",
                "172",
                "77",
                "19BBY",
                List.of("f1", "f2", "f3", "f4"),
                "2014-12-09T13:50:51.644000Z");

        when(swapiClient.getPersonById(1)).thenReturn(luke);

        mockMvc.perform(get("/people/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.height").value(1.72))
                .andExpect(jsonPath("$.mass").value(77.0))
                .andExpect(jsonPath("$.birth_year").value("19BBY"))
                .andExpect(jsonPath("$.number_of_films").value(4))
                .andExpect(jsonPath("$.date_added").value("09-12-2014"));
    }

    @Test
    void getPersonByIdParsesJabbaMassWithComma() throws Exception {
        SwapiPersonResponse jabba = fullPerson(
                "Jabba Desilijic Tiure",
                "175",
                "1,358",
                "600BBY",
                List.of("f1", "f2", "f3"),
                "2014-12-10T16:11:31.638000Z");

        when(swapiClient.getPersonById(16)).thenReturn(jabba);

        mockMvc.perform(get("/people/16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mass").value(1358.0))
                .andExpect(jsonPath("$.height").value(1.75));
    }

    @Test
    void getPersonByIdReturnsNullMassForUnknown() throws Exception {
        SwapiPersonResponse tarkin = fullPerson(
                "Wilhuff Tarkin", "180", "unknown", "64BBY", List.of("f1", "f2"), "2014-12-10T16:26:56.138000Z");

        when(swapiClient.getPersonById(12)).thenReturn(tarkin);

        mockMvc.perform(get("/people/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mass").value(nullValue()))
                .andExpect(jsonPath("$.height").value(1.8));
    }

    @Test
    void getPersonByIdReturns404WhenNotFound() throws Exception {
        when(swapiClient.getPersonById(999)).thenThrow(new PersonNotFoundException("Person not found: 999"));

        mockMvc.perform(get("/people/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Person not found: 999"));
    }

    @Test
    void getPersonByIdRejectsInvalidId() throws Exception {
        mockMvc.perform(get("/people/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id must be >= 1"));

        mockMvc.perform(get("/people/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id must be >= 1"));
    }

    @Test
    void getPersonByIdCachesDetails() throws Exception {
        SwapiPersonResponse luke = fullPerson(
                "Luke Skywalker", "172", "77", "19BBY", List.of("f1"), "2014-12-09T13:50:51.644000Z");
        when(swapiClient.getPersonById(1)).thenReturn(luke);

        mockMvc.perform(get("/people/1")).andExpect(status().isOk());
        mockMvc.perform(get("/people/1")).andExpect(status().isOk());

        verify(swapiClient, times(1)).getPersonById(1);
    }

    private static SwapiPersonResponse person(String name, String url) {
        SwapiPersonResponse person = new SwapiPersonResponse();
        person.setName(name);
        person.setUrl(url);
        return person;
    }

    private static SwapiPeoplePageResponse pageResponse(
            int count, String next, String previous, List<SwapiPersonResponse> results) {
        SwapiPeoplePageResponse page = new SwapiPeoplePageResponse();
        page.setCount(count);
        page.setNext(next);
        page.setPrevious(previous);
        page.setResults(results);
        return page;
    }

    private static SwapiPersonResponse fullPerson(
            String name,
            String height,
            String mass,
            String birthYear,
            List<String> films,
            String created) {
        SwapiPersonResponse person = new SwapiPersonResponse();
        person.setName(name);
        person.setHeight(height);
        person.setMass(mass);
        person.setBirthYear(birthYear);
        person.setFilms(films);
        person.setCreated(created);
        return person;
    }
}
