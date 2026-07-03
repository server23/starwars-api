package com.example.starwars_api.controller;

import com.example.starwars_api.dto.PersonDetailDto;
import com.example.starwars_api.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PeopleController.class)
class PeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @Test
    void getPersonByIdReturnsFormattedDetails() throws Exception {
        when(personService.getPersonById(1)).thenReturn(PersonDetailDto.builder()
                .name("Luke Skywalker")
                .height(1.72)
                .mass(77.0)
                .birthYear("19BBY")
                .numberOfFilms(4)
                .dateAdded("09-12-2014")
                .build());

        mockMvc.perform(get("/people/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.height").value(1.72))
                .andExpect(jsonPath("$.mass").value(77.0))
                .andExpect(jsonPath("$.birth_year").value("19BBY"))
                .andExpect(jsonPath("$.number_of_films").value(4))
                .andExpect(jsonPath("$.date_added").value("09-12-2014"));
    }
}
