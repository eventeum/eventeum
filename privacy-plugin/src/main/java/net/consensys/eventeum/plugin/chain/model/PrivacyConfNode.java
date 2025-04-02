package net.consensys.eventeum.plugin.chain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrivacyConfNode {
  private boolean enabled;
  private String privacyPrecompiledAddress;
}
