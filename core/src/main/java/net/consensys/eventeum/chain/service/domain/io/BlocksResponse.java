package net.consensys.eventeum.chain.service.domain.io;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlocksResponse {
  List<BlockResponse> blocks;
  Map<String, String> links;
}
