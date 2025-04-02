package net.consensys.eventeum.integration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hedera")
public class HederaSettings {

  private boolean testnet;
  private Account account = new Account();

  @Data
  public static class Account {
    private String id;
    private String privateKey;
  }
}
