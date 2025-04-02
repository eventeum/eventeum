package net.consensys.eventeum.chain.settings;

public enum ChainType {
  ETHEREUM("ethereum"),
  HASHGRAPH("hashgraph");

  private final String chainName;

  ChainType(String chainName) {
    this.chainName = chainName;
  }

  public String getChainName() {
    return chainName;
  }
}
