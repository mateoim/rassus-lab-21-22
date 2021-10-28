package hr.fer.tel.rassus.server.controllers;

import hr.fer.tel.rassus.server.beans.Sensor;
import hr.fer.tel.rassus.server.services.SensorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/sensors")
public class SensorsController {

    private final SensorRepository sensorRepository;

    public SensorsController(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @PutMapping
    public ResponseEntity<Sensor> registerSensor(@RequestBody Sensor sensor) {
        sensorRepository.save(sensor);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(sensor.getId()).toUri();

        return ResponseEntity.created(location).body(sensor);
    }

    @GetMapping
    public List<Sensor> getSensors() {
        return sensorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Sensor getSensor(@PathVariable Long id) {
        return sensorRepository.findById(id).orElseThrow();
    }

    //  TODO 4.2  Najbliži susjed

}
