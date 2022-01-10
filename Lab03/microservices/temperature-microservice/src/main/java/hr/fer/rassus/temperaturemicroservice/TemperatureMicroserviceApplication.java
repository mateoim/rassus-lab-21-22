package hr.fer.rassus.temperaturemicroservice;

import hr.fer.rassus.temperaturemicroservice.beans.Reading;
import hr.fer.rassus.temperaturemicroservice.services.ReadingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@EnableDiscoveryClient
@SpringBootApplication
public class TemperatureMicroserviceApplication implements CommandLineRunner {

    private final ReadingRepository readingRepository;

    public TemperatureMicroserviceApplication(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(TemperatureMicroserviceApplication.class, args);
    }

    @Override
    public void run(String[] args) throws Exception {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        final List<String> lines = Files.readAllLines(Paths.get(Objects.requireNonNull(
                classloader.getResource("readings[2].csv")).toURI()));

        lines.remove(0);

        for (String line : lines) {
            final String[] parts = line.split(",");

            final Reading reading = new Reading();
            reading.setValue(Double.parseDouble(parts[0]));

            readingRepository.save(reading);
        }
    }
}
