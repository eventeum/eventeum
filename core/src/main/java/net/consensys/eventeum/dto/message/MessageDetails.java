package net.consensys.eventeum.dto.message;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Entity
@Document
@NoArgsConstructor
public class MessageDetails {

  @Id @GeneratedValue private String id;

  private String nodeName;

  private String topicId;

  private String message;

  private Long timestamp;

  private Long sequenceNumber;

  private byte[] runningHash;

  public MessageDetails(
      String nodeName,
      String topicId,
      String message,
      Long timestamp,
      Long sequenceNumber,
      byte[] runningHash) {
    this.nodeName = nodeName;
    this.topicId = topicId;
    this.message = message;
    this.timestamp = timestamp;
    this.sequenceNumber = sequenceNumber;
    this.runningHash = runningHash;
  }
}
