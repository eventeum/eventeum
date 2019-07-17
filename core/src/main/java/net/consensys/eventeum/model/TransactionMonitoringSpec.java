package net.consensys.eventeum.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.crypto.Hash;

import java.util.List;

@Data
@NoArgsConstructor
public class TransactionMonitoringSpec {

    private String id;

    private TransactionIdentifierType type;

    private String transactionIdentifier;

    private String nodeName;

    private List<String> statuses;

    private String transactionIdentifierValue;

    public TransactionMonitoringSpec(TransactionIdentifierType type,
                                     String transactionIdentifier,
                                     String nodeName) {
        this.type = type;
        this.transactionIdentifier = transactionIdentifier;
        this.nodeName = nodeName;

        this.id = Hash.sha3String(transactionIdentifier + type + nodeName).substring(2);
    }

    public TransactionMonitoringSpec(TransactionIdentifierType type,
                                     String transactionIdentifierValue,
                                     String nodeName,
                                     List<String> statuses) {
        this.type = type;
        this.transactionIdentifierValue = transactionIdentifierValue;
        this.nodeName = nodeName;
        this.statuses = statuses;

        this.id = Hash.sha3String(transactionIdentifierValue + type + nodeName + statuses.toString()).substring(2);
    }
}
