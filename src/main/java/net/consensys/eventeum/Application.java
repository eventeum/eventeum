package net.consensys.eventeum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableFeignClients
@Profile("default")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
