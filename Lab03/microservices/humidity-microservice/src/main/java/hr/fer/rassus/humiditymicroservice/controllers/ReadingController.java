package hr.fer.rassus.humiditymicroservice.controllers;

import hr.fer.rassus.humiditymicroservice.beans.Reading;
import hr.fer.rassus.humiditymicroservice.services.ReadingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/readings")
public class ReadingController {

    private final ReadingRepository readingRepository;

    public ReadingController(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    @GetMapping("/latest")
    public ResponseEntity<Reading> getReading() {
        final long id = (System.currentTimeMillis() % 100) + 1;

        Optional<Reading> query = readingRepository.findById(id);

        if (query.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().body(query.get());
        }
    }
}
