package hr.fer.rassus.temperaturemicroservice.services;

import hr.fer.rassus.temperaturemicroservice.beans.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, Long> {}
