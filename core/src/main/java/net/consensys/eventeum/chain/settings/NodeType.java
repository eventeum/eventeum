package net.consensys.eventeum.chain.settings;

public enum NodeType {
  NORMAL("NORMAL"),
  MIRROR("MIRROR");

  private final String nodeName;

  NodeType(String nodeName) {
    this.nodeName = nodeName;
  }

  public String getNodeName() {
    return nodeName;
  }
}
