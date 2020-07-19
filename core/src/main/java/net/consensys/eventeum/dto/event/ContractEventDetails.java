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

package net.consensys.eventeum.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import net.consensys.eventeum.dto.TransactionBasedDetails;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.consensys.eventeum.dto.event.parameter.EventParameter;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Represents the details of an emitted Ethereum smart contract event.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Document
@Entity
@Data
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractEventDetails implements TransactionBasedDetails {

    private String name;

    @Id
    private String filterId;

    private String nodeName;

    @Lob // required because of https://stackoverflow.com/questions/43412517/sql-string-or-binary-data-would-be-truncated-error/43426863
    @ElementCollection
    private List<EventParameter> indexedParameters;

    @Lob
    @ElementCollection
    private List<EventParameter> nonIndexedParameters;

    private String transactionHash;

    private BigInteger logIndex;

    private BigInteger blockNumber;

    private String blockHash;

    private String address;

    private ContractEventStatus status = ContractEventStatus.UNCONFIRMED;

    private String eventSpecificationSignature;

    private String networkName;

    private BigInteger timestamp;

    public String getId() {
        return transactionHash + "-" + blockHash + "-" + logIndex;
    }
}
