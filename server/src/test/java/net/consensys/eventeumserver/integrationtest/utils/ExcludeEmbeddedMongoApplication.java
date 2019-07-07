package net.consensys.eventeumserver.integrationtest.utils;

import net.consensys.eventeum.annotation.EnableEventeum;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;

//@SpringBootApplication(exclude = {EmbeddedMongoAutoConfiguration.class})
//@EnableEventeum
public class ExcludeEmbeddedMongoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcludeEmbeddedMongoApplication.class, args);
    }
}