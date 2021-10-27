package hr.fer.tel.rassus.server.services;

import hr.fer.tel.rassus.server.beans.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {}
