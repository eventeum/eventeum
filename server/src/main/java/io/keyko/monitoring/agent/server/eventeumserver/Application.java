package io.keyko.monitoring.agent.server.eventeumserver;

import io.keyko.monitoring.agent.core.annotation.EnableEventeum;
import io.keyko.monitoring.agent.core.config.DatabaseConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEventeum
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
