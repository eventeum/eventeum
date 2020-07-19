/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.transaction.TransactionStatus;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;

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
