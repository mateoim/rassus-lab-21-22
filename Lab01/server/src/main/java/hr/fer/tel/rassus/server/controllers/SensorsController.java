package hr.fer.tel.rassus.server.controllers;

import hr.fer.tel.rassus.server.beans.Sensor;
import hr.fer.tel.rassus.server.services.SensorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sensors")
public class SensorsController {

    private final SensorRepository sensorRepository;

    private static final int R = 6371;

    public SensorsController(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @PostMapping
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
    public ResponseEntity<Sensor> getSensor(@PathVariable Long id) {
        Optional<Sensor> query = sensorRepository.findById(id);

        if (query.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(query.get());
        }
    }

    @GetMapping("/closest/{id}")
    public ResponseEntity<Sensor> findClosest(@PathVariable Long id) {
        Optional<Sensor> query = sensorRepository.findById(id);

        if (query.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            Sensor current = query.get();
            Sensor closest = null;
            double distance = Double.POSITIVE_INFINITY;
            List<Sensor> sensors = sensorRepository.findAll();

            for (Sensor sensor : sensors) {
                if (current.getId() == sensor.getId()) continue;

                double dlon = sensor.getLongitude() - current.getLongitude();
                double dlat = sensor.getLatitude() - current.getLatitude();
                double a = Math.pow(Math.sin(dlat / 2), 2) +
                        Math.cos(current.getLatitude()) * Math.cos(sensor.getLatitude()) *
                                Math.pow(Math.sin(dlon / 2), 2);
                double c = Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double d = R * c;

                if (d < distance) {
                    distance = d;
                    closest = sensor;
                }
            }

            if (closest != null) {
                return ResponseEntity.ok(closest);
            } else {
                return ResponseEntity.noContent().build();
            }
        }
    }
}
