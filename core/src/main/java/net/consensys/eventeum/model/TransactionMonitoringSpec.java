package net.consensys.eventeum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.crypto.Hash;

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

        this.id = Hash.sha3String(transactionIdentifier + type + nodeName).substring(2);
    }

}
