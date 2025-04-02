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

package net.consensys.eventeum.dto.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;
import java.util.Arrays;
import lombok.*;
import net.consensys.eventeum.dto.TransactionBasedDetails;

@Data
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails implements TransactionBasedDetails {

  private String hash;
  private String nonce;
  private String blockHash;
  private String blockNumber;
  private String blockTimestamp;
  private String transactionIndex;
  private String from;
  private String to;
  private String value;
  private String nodeName;
  private String contractAddress;
  private String input;
  private String revertReason;
  private BigInteger timestamp;

  private TransactionStatus status;

  @JsonIgnore
  public String getTransactionHash() {
    return hash;
  }

  public boolean isSuccess() {
    return status != null
        && Arrays.asList(TransactionStatus.CONFIRMED, TransactionStatus.UNCONFIRMED)
            .contains(status);
  }
}
