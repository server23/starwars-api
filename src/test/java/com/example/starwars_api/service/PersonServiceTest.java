package com.example.starwars_api.service;

import com.example.starwars_api.client.SwapiClient;
import com.example.starwars_api.dto.PersonDetailDto;
import com.example.starwars_api.dto.swapi.SwapiPersonResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import com.example.starwars_api.dto.PeoplePageDto;
import com.example.starwars_api.dto.swapi.SwapiPeoplePageResponse;
import com.example.starwars_api.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private SwapiClient swapiClient;

    @InjectMocks
    private PersonService personService;

    @Test
    void getPersonByIdParsesMassWithComma() {
        SwapiPersonResponse jabba = new SwapiPersonResponse();
        jabba.setName("Jabba Desilijic Tiure");
        jabba.setHeight("175");
        jabba.setMass("1,358");
        jabba.setBirthYear("600BBY");
        jabba.setFilms(List.of("https://swapi.dev/api/films/3/"));
        jabba.setCreated("2014-12-10T16:32:32.407000Z");

        when(swapiClient.getPersonById(16)).thenReturn(jabba);

        PersonDetailDto result = personService.getPersonById(16);

        assertThat(result.getName()).isEqualTo("Jabba Desilijic Tiure");
        assertThat(result.getMass()).isEqualTo(1358.0);
        assertThat(result.getHeight()).isEqualTo(1.75);
    }

    @Test
    void getPeopleRejectsInvalidPage() {
        assertThatThrownBy(() -> personService.getPeople(0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Page must be >= 1");
    }

    @Test
    void getPersonByIdRejectsInvalidId() {
        assertThatThrownBy(() -> personService.getPersonById(0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Id must be >= 1");
    }

    @Test
    void getPersonByIdReturnsNullMassForUnknown() {
        SwapiPersonResponse tarkin = new SwapiPersonResponse();
        tarkin.setName("Wilhuff Tarkin");
        tarkin.setHeight("180");
        tarkin.setMass("unknown");
        tarkin.setBirthYear("64BBY");
        tarkin.setFilms(List.of());
        tarkin.setCreated("2014-12-10T16:26:56.138000Z");

        when(swapiClient.getPersonById(12)).thenReturn(tarkin);

        PersonDetailDto result = personService.getPersonById(12);

        assertThat(result.getMass()).isNull();
        assertThat(result.getHeight()).isEqualTo(1.8);
    }

    @Test
    void getPeopleCachesPages() {
        SwapiPeoplePageResponse page = new SwapiPeoplePageResponse();
        page.setCount(82);
        page.setResults(List.of());

        when(swapiClient.getPeoplePage(1)).thenReturn(page);

        PeoplePageDto first = personService.getPeople(1);
        PeoplePageDto second = personService.getPeople(1);

        assertThat(first).isSameAs(second);
        verify(swapiClient, times(1)).getPeoplePage(1);
    }

    @Test
    void getPersonByIdCachesDetails() {
        SwapiPersonResponse luke = new SwapiPersonResponse();
        luke.setName("Luke Skywalker");
        luke.setHeight("172");
        luke.setMass("77");
        luke.setBirthYear("19BBY");
        luke.setFilms(List.of("film"));
        luke.setCreated("2014-12-09T13:50:51.644000Z");

        when(swapiClient.getPersonById(1)).thenReturn(luke);

        PersonDetailDto first = personService.getPersonById(1);
        PersonDetailDto second = personService.getPersonById(1);

        assertThat(first).isSameAs(second);
        verify(swapiClient, times(1)).getPersonById(1);
    }
}
