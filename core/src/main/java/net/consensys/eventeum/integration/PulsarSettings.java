package net.consensys.eventeum.integration;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "pulsar")
@Data
public class PulsarSettings {
	@Data
	public static class Authentication {
		private String pluginClassName;
		private Map<String, String> params;
	}

	private Map<String, Object> config;
	private Authentication authentication;
	private String topic;
}
