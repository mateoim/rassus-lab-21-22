package hr.fer.rassus.aggregatormicroservice.controllers;

import hr.fer.rassus.aggregatormicroservice.model.Reading;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableAutoConfiguration
@RestController
public class AggregatorController {

    private static final double KELVIN = 273.15;

    private final RestTemplate restTemplate;

    @Value("${config.temperature-unit}")
    private String temperatureUnit = "C";

    public AggregatorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/latest-reading")
    public ResponseEntity<Set<Reading>> getStudents() {
        ResponseEntity<Reading> humidity =
                restTemplate.getForEntity("http://humidity-microservice/readings/latest", Reading.class);
        ResponseEntity<Reading> temperature =
                restTemplate.getForEntity("http://temperature-microservice/readings/latest", Reading.class);

        if (humidity.getStatusCode() != HttpStatus.OK || temperature.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.noContent().build();
        }

        Reading temperatureBody = temperature.getBody();

        if (this.temperatureUnit.equals("K") && temperatureBody != null && temperatureBody.getUnit().equals("C")) {
            temperatureBody.setUnit("K");
            temperatureBody.setValue(temperatureBody.getValue() + KELVIN);
        }

        Set<Reading> response = new HashSet<>();
        response.add(humidity.getBody());
        response.add(temperatureBody);

        return ResponseEntity.ok().body(response);
    }
}
