package io.keyko.monitoring.agent.server.eventeumserver.integrationtest.utils;

import io.keyko.monitoring.agent.core.annotation.EnableEventeum;
import org.springframework.boot.SpringApplication;

//@SpringBootApplication(exclude = {EmbeddedMongoAutoConfiguration.class})
//@EnableEventeum
public class ExcludeEmbeddedMongoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcludeEmbeddedMongoApplication.class, args);
    }
}