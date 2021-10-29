package hr.fer.tel.rassus.server.controllers;

import hr.fer.tel.rassus.server.beans.Reading;
import hr.fer.tel.rassus.server.beans.Sensor;
import hr.fer.tel.rassus.server.services.ReadingRepository;
import hr.fer.tel.rassus.server.services.SensorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/readings")
public class ReadingController {

    private final ReadingRepository readingRepository;

    private final SensorRepository sensorRepository;

    public ReadingController(ReadingRepository readingRepository, SensorRepository sensorRepository) {
        this.readingRepository = readingRepository;
        this.sensorRepository = sensorRepository;
    }

    @PutMapping("/sensor/{id}")
    public ResponseEntity<Reading> saveReading(@RequestBody Reading reading, @PathVariable long id) {
        Optional<Sensor> query = sensorRepository.findById(id);

        if (query.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            Sensor sensor = query.get();
            reading.setSensor(sensor);
            readingRepository.save(reading);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().replacePath("/readings/{id}")
                    .build(reading.getId());

            return ResponseEntity.created(location).body(reading);
        }
    }

    @GetMapping("/sensor/{id}")
    public ResponseEntity<Set<Reading>> getSensorReadings(@PathVariable long id) {
        Optional<Sensor> query = sensorRepository.findById(id);

        if (query.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().body(query.get().getReadings());
        }
    }

    @GetMapping
    public List<Reading> getReadings() {
        return readingRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reading> getReading(@PathVariable Long id) {
        Optional<Reading> query = readingRepository.findById(id);

        if (query.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().body(query.get());
        }
    }
}