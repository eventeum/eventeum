package net.consensys.eventeum.dto.message;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MessageEvent extends AbstractMessage<MessageDetails> {

  public static final String TYPE = "MESSAGE";

  public MessageEvent(MessageDetails details) {
    super(details.getTopicId(), TYPE, details);
  }
}
