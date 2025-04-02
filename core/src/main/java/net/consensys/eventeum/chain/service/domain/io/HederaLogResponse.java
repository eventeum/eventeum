package net.consensys.eventeum.chain.service.domain.io;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HederaLogResponse {

  private String address;
  private String bloom;
  private String contract_id;
  private String data;
  private String index;
  private List<String> topics;
}
