package net.consensys.eventeum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransactionMonitoringSpec {

    private String id;

    private TransactionIdentifierType type;

    private String transactionIdentifier;

    private String nodeName;

    public TransactionMonitoringSpec(TransactionIdentifierType type,
                                     String transactionIdentifier,
                                     String nodeName) {
        this.type = type;
        this.transactionIdentifier = transactionIdentifier;
        this.nodeName = nodeName;
    }

    public String getId() {

        if (id != null) {
            return id;
        }

        return transactionIdentifier + "-" + type + "-" + nodeName;
    }

    public void setId(String id) {
        this.id = id;
    }

}
