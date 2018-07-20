package net.consensys.eventeum.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableFeignClients({BaseConfiguration.BASE_PACKAGE})
@ComponentScan({BaseConfiguration.BASE_PACKAGE})
@EnableMongoRepositories(basePackages = {BaseConfiguration.BASE_PACKAGE})
@Configuration
public class BaseConfiguration {
    public static final String BASE_PACKAGE = "net.consensys.eventeum";
}
