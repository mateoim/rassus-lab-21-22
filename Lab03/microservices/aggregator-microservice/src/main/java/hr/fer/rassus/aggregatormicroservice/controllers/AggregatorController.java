package hr.fer.rassus.aggregatormicroservice.controllers;

import hr.fer.rassus.aggregatormicroservice.model.Reading;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@RestController
public class AggregatorController {
    private final RestTemplate restTemplate;

    public AggregatorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/latest-reading")
    public ResponseEntity<Set<Reading>> getStudents() {
        ResponseEntity<Reading> humidity =
                restTemplate.getForEntity("http://humidity-microservice/readings/latest", Reading.class);
        ResponseEntity<Reading> temperature =
                restTemplate.getForEntity("http://temperature-microservice/readings/latest", Reading.class);

        Set<Reading> response = new HashSet<>();
        response.add(humidity.getBody());
        response.add(temperature.getBody());

        return ResponseEntity.ok().body(response);
    }
}
