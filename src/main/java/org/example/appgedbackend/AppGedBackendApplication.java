package org.example.appgedbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppGedBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppGedBackendApplication.class, args);
    }

}
