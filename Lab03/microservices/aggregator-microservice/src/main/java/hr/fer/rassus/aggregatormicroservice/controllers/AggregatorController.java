package hr.fer.rassus.aggregatormicroservice.controllers;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import hr.fer.rassus.aggregatormicroservice.config.UnitConfiguration;
import hr.fer.rassus.aggregatormicroservice.model.Reading;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EurekaClient eurekaClient;

    private final RestTemplate restTemplate;

    private final UnitConfiguration temperatureUnit;

    public AggregatorController(RestTemplate restTemplate, UnitConfiguration temperatureUnit) {
        this.restTemplate = restTemplate;
        this.temperatureUnit = temperatureUnit;
    }

    @GetMapping("/latest-reading")
    public ResponseEntity<Set<Reading>> getStudents() {
        final Application humidityApp = eurekaClient.getApplication("humidity-microservice");

        if (humidityApp == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<Reading> humidity =
                restTemplate.getForEntity("http://humidity-microservice/readings/latest", Reading.class);

        Application temperatureApp = eurekaClient.getApplication("temperature-microservice");

        if (temperatureApp == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<Reading> temperature =
                restTemplate.getForEntity("http://temperature-microservice/readings/latest", Reading.class);

        if (humidity.getStatusCode() != HttpStatus.OK || temperature.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.notFound().build();
        }

        Reading temperatureBody = temperature.getBody();

        if (this.temperatureUnit.getUnit().equals("K")
                && temperatureBody != null && temperatureBody.getUnit().equals("C")) {
            temperatureBody.setUnit("K");
            temperatureBody.setValue(temperatureBody.getValue() + KELVIN);
        }

        Set<Reading> response = new HashSet<>();
        response.add(humidity.getBody());
        response.add(temperatureBody);

        return ResponseEntity.ok().body(response);
    }
}
