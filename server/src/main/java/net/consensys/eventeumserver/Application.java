package net.consensys.eventeumserver;

import net.consensys.eventeum.annotation.EnableEventeum;
import net.consensys.kafkadl.EnableKafkaDeadLetter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEventeum
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
