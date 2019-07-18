package net.consensys.eventeum.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import org.web3j.crypto.Hash;

import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class TransactionMonitoringSpec {

    private String id;

    private TransactionIdentifierType type;

    private String nodeName;

    private List<TransactionStatus> statuses = Arrays.asList(TransactionStatus.CONFIRMED, TransactionStatus.FAILED);

    private String transactionIdentifierValue;

    public TransactionMonitoringSpec(TransactionIdentifierType type,
                                     String transactionIdentifierValue,
                                     String nodeName,
                                     List<TransactionStatus> statuses) {
        this.type = type;
        this.transactionIdentifierValue = transactionIdentifierValue;
        this.nodeName = nodeName;
        this.statuses = statuses;

        this.id = Hash.sha3String(transactionIdentifierValue + type + nodeName + statuses.toString()).substring(2);
    }

    public TransactionMonitoringSpec(TransactionIdentifierType type,
                                     String transactionIdentifierValue,
                                     String nodeName) {
        this.type = type;
        this.transactionIdentifierValue = transactionIdentifierValue;
        this.nodeName = nodeName;

        this.id = Hash.sha3String(transactionIdentifierValue + type + nodeName + statuses.toString()).substring(2);
    }


    @JsonSetter("type")
    public void setType(String type) {
        this.type = TransactionIdentifierType.valueOf(type.toUpperCase());
    }

    @JsonSetter("type")
    public void setType(TransactionIdentifierType type) {
        this.type = type;
    }

    public void generateId() {
        this.id = Hash.sha3String(transactionIdentifierValue + type + nodeName + statuses.toString()).substring(2);
    }
}
