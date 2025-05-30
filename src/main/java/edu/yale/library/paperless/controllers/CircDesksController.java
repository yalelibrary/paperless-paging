package edu.yale.library.paperless.controllers;

import edu.yale.library.paperless.entities.CirculationDesk;
import edu.yale.library.paperless.repositories.CirculationDeskRepository;
import edu.yale.library.paperless.services.UnauthorizedRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CircDesksController {

    private final CirculationDeskRepository circulationDeskRepository;

    @RequestMapping(method = RequestMethod.GET, value = "/circDesks")
    public Iterable<CirculationDesk> listCircDesks() throws UnauthorizedRequestException {
        return circulationDeskRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/task-location-list")
    public List<String> locationList() throws UnauthorizedRequestException {
        return StreamSupport.stream(circulationDeskRepository.findAll().spliterator(), false).map((circulationDesk)-> {
            return circulationDesk.getName();
        }).collect(Collectors.toList());
    }

}
