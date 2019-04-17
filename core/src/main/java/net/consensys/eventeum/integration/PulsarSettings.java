package net.consensys.eventeum.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "pulsar")
@Data
public class PulsarSettings {
	private String url;
	private String topic;
}
