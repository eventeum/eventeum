package net.consensys.eventeum.chain.service.domain.io;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigInteger;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BlockResponse {
  private Integer count;
  private BigInteger gasUsed;
  private String hapiVersion;
  private String hash;
  private String logsBloom;
  private String name;
  private BigInteger number;
  private String previousHash;
  private BigInteger size;
  private TimestampResponse timestamp;
}
