package net.consensys.eventeum.chain.service.domain.io;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimestampResponse {
  String from;
  String to;
}
