package net.consensys.eventeum.chain.block.message;

import net.consensys.eventeum.dto.message.MessageDetails;

public interface MessageListener {

  void onMessage(MessageDetails message);
}
