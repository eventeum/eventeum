package net.consensys.eventeum.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients({BaseConfiguration.BASE_PACKAGE})
@ComponentScan({BaseConfiguration.BASE_PACKAGE})
@EntityScan(basePackages = {BaseConfiguration.BASE_PACKAGE})
@Configuration
public class BaseConfiguration {
    public static final String BASE_PACKAGE = "net.consensys.eventeum";
}
