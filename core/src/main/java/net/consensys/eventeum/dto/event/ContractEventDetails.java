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
import jakarta.persistence.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.consensys.eventeum.dto.TransactionBasedDetails;
import net.consensys.eventeum.dto.converter.EventParameterConverter;
import net.consensys.eventeum.dto.converter.HashMapConverter;
import net.consensys.eventeum.dto.event.parameter.EventParameter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

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

  @Id @GeneratedValue private UUID id;

  @MongoId
  @Field("_id")
  private String filterId;

  private String name;

  private String nodeName;

  @ElementCollection
  @Convert(converter = EventParameterConverter.class)
  private List<EventParameter> indexedParameters;

  @ElementCollection
  @Convert(converter = EventParameterConverter.class)
  private List<EventParameter> nonIndexedParameters;

  private String transactionHash;

  private BigInteger logIndex;

  private BigInteger blockNumber;

  private String blockHash;

  private String address;

  @Column(name = "\"from\"")
  private String from;

  private ContractEventStatus status = ContractEventStatus.UNCONFIRMED;

  private String eventSpecificationSignature;

  private String networkName;

  private BigInteger timestamp;

  private BigInteger blockTimestamp;

  @Convert(converter = HashMapConverter.class)
  private Map<String, Object> extensionData;

  public String getEventIdentifier() {
    return transactionHash + "-" + blockHash + "-" + logIndex;
  }
}
