package com.example.starwars_api.controller;

import com.example.starwars_api.dto.PeoplePageDto;
import com.example.starwars_api.dto.PersonDetailDto;
import com.example.starwars_api.service.PersonService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final PersonService personService;

    public PeopleController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public PeoplePageDto getPeople(@RequestParam(defaultValue = "1") int page) {
        return personService.getPeople(page);
    }

    @GetMapping("/{id}")
    public PersonDetailDto getPersonById(@PathVariable int id) {
        return personService.getPersonById(id);
    }
}
