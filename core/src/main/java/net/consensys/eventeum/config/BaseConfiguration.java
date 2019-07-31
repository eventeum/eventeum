package net.consensys.eventeum.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients({BaseConfiguration.BASE_PACKAGE})
@ComponentScan({BaseConfiguration.BASE_PACKAGE})
//@EnableMongoRepositories(basePackages = {BaseConfiguration.BASE_PACKAGE})
//@EnableJpaRepositories(basePackages = {BaseConfiguration.BASE_PACKAGE})
@EntityScan(basePackages = {BaseConfiguration.BASE_PACKAGE})
@Configuration
public class BaseConfiguration {
    public static final String BASE_PACKAGE = "net.consensys.eventeum";
}
