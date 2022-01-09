package hr.fer.rassus.humiditymicroservice.services;

import hr.fer.rassus.humiditymicroservice.beans.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, Long> {}
