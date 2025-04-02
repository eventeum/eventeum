package net.consensys.eventeum.plugin.chain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrivacyConfFilter {
  private boolean enabled;
  private String privacyGroupID;
}
