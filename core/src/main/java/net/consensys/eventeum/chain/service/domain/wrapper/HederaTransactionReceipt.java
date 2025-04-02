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

package net.consensys.eventeum.chain.service.domain.wrapper;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import net.consensys.eventeum.chain.service.domain.Log;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.chain.service.domain.io.ContractResultResponse;

/**
 * A TransactionReceipt that is constructed from a Web3j transaction receipt.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
public class HederaTransactionReceipt implements TransactionReceipt {

  private String transactionHash;
  private BigInteger transactionIndex;
  private String blockHash;
  private BigInteger blockNumber;
  private BigInteger cumulativeGasUsed;
  private BigInteger gasUsed;
  private String contractAddress;
  private String root = "";
  private String from;
  private String to;
  private List<Log> logs = Collections.emptyList();
  private String logsBloom = "";
  private String status;
  private String revertReason;

  public HederaTransactionReceipt(ContractResultResponse response) {
    this.transactionHash = response.getHash();
    this.transactionIndex = response.getTransactionIndex();
    this.blockHash = response.getBlockHash();
    this.cumulativeGasUsed = response.getBlockGasUsed();
    this.gasUsed = response.getGasUsed();
    this.contractAddress = response.getContractId();
    this.from = response.getFrom();
    this.to = response.getTo();
    this.status = response.getStatus();
  }
}
