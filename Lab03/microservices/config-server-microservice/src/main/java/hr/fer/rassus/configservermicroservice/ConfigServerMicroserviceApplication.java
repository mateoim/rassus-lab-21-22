package hr.fer.rassus.configservermicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerMicroserviceApplication.class, args);
    }

}
