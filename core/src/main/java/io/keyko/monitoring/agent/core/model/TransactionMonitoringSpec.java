package io.keyko.monitoring.agent.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import io.keyko.monitoring.agent.core.constant.Constants;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import org.springframework.data.mongodb.core.mapping.Document;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document
@Entity
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class TransactionMonitoringSpec {

    @Id
    private String id;

    private TransactionIdentifierType type;

    private String nodeName = Constants.DEFAULT_NODE_NAME;

    //Need to wrap in an ArrayList so its modifiable
    @ElementCollection
    @Enumerated(EnumType.ORDINAL)
    private List<TransactionStatus> statuses = new ArrayList(
            Arrays.asList(TransactionStatus.UNCONFIRMED, TransactionStatus.CONFIRMED, TransactionStatus.FAILED));

    private String transactionIdentifierValue;

    public TransactionMonitoringSpec(TransactionIdentifierType type,
                                     String transactionIdentifierValue,
                                     String nodeName,
                                     List<TransactionStatus> statuses) {
        this.type = type;
        this.transactionIdentifierValue = transactionIdentifierValue;
        this.nodeName = nodeName;

        if (statuses != null && !statuses.isEmpty()) {
            this.statuses = statuses;
        }

        convertToCheckSum();

        this.id = Hash.sha3String(transactionIdentifierValue + type + nodeName + this.statuses.toString()).substring(2);
    }

    public TransactionMonitoringSpec(TransactionIdentifierType type,
                                     String transactionIdentifierValue,
                                     String nodeName) {
        this(type, transactionIdentifierValue, nodeName, null);
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

    public void convertToCheckSum() {
        if (this.type != TransactionIdentifierType.HASH) {
            this.transactionIdentifierValue = Keys.toChecksumAddress(this.transactionIdentifierValue);
        }
    }
}
