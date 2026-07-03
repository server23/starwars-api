package com.example.starwars_api.service;

import com.example.starwars_api.client.SwapiClient;
import com.example.starwars_api.dto.*;
import com.example.starwars_api.dto.swapi.SwapiPersonResponse;
import com.example.starwars_api.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PersonService {

    private final SwapiClient swapiClient;
    private final Map<Integer, PeoplePageDto> pageCache = new ConcurrentHashMap<>();
    private final Map<Integer, PersonDetailDto> personCache = new ConcurrentHashMap<>();

    public PersonService(SwapiClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    public PeoplePageDto getPeople(int page) {
        if (page < 1) throw new BadRequestException("Page must be >= 1");

        return pageCache.computeIfAbsent(page, p -> {
            var swapiPage = swapiClient.getPeoplePage(p);
            List<PersonSummaryDto> results = swapiPage.getResults().stream()
                    .map(person -> PersonSummaryDto.builder()
                            .id(extractId(person.getUrl()))
                            .name(person.getName())
                            .build())
                    .toList();

            return PeoplePageDto.builder()
                    .count(swapiPage.getCount())
                    .next(swapiPage.getNext())
                    .previous(swapiPage.getPrevious())
                    .results(results)
                    .build();
        });
    }

    public PersonDetailDto getPersonById(int id) {
        if (id < 1) throw new BadRequestException("Id must be >= 1");

        return personCache.computeIfAbsent(id, i -> mapToDetail(swapiClient.getPersonById(i)));
    }

    private PersonDetailDto mapToDetail(SwapiPersonResponse person) {
        return PersonDetailDto.builder()
                .name(person.getName())
                .height(parseHeight(person.getHeight()))
                .mass(parseMass(person.getMass()))
                .birthYear(person.getBirthYear())
                .numberOfFilms(person.getFilms() != null ? person.getFilms().size() : 0)
                .dateAdded(formatDate(person.getCreated()))
                .build();
    }

    private Double parseHeight(String height) {
        if (height == null || height.equalsIgnoreCase("unknown")) {
            return null;
        }
        return parseNumber(height) / 100.0;
    }

    private Double parseMass(String mass) {
        if (mass == null || mass.equalsIgnoreCase("unknown")) {
            return null;
        }
        return parseNumber(mass);
    }

    private double parseNumber(String value) {
        String normalized = value.replace(",", "").trim();
        return Double.parseDouble(normalized);
    }

    private String formatDate(String created) {
        LocalDate date = LocalDate.parse(created.substring(0, 10));
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private int extractId(String url) {
        String[] parts = url.replaceAll("/$", "").split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
