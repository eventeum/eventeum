package net.consensys.eventeum.chain.service.domain.io;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StateChangeResponse {

  private String address;

  private String contractId;

  private String slot;

  private String valueRead;

  private String valueWritten;
}
