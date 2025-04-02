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
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.utils.ModelMapperFactory;
import org.modelmapper.ModelMapper;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;

@Data
@Builder
@AllArgsConstructor
public class Web3jBlock implements Block {

  private BigInteger number;
  private String hash;
  private String parentHash;
  private BigInteger nonce;
  private String sha3Uncles;
  private String logsBloom;
  private String transactionsRoot;
  private String stateRoot;
  private String receiptsRoot;
  private String author;
  private String miner;
  private String mixHash;
  private BigInteger difficulty;
  private BigInteger totalDifficulty;
  private String extraData;
  private BigInteger size;
  private BigInteger gasLimit;
  private BigInteger gasUsed;
  private BigInteger timestamp;
  private List<Transaction> transactions;
  private List<String> uncles;
  private List<String> sealFields;
  private String nodeName;

  public Web3jBlock(EthBlock.Block web3jBlock, String nodeName) {
    final ModelMapper modelMapper = ModelMapperFactory.getInstance().getModelMapper();

    modelMapper.map(web3jBlock, this);

    transactions = convertTransactions(web3jBlock.getTransactions());

    this.nodeName = nodeName;
  }

  public Web3jBlock(BigInteger number, String hash, BigInteger timestamp, String nodeName) {
    this.number = number;
    this.hash = hash;
    this.timestamp = timestamp;
    this.nodeName = nodeName;
  }

  private List<Transaction> convertTransactions(List<EthBlock.TransactionResult> toConvert) {
    return toConvert.stream()
        .map(
            tx -> {
              org.web3j.protocol.core.methods.response.Transaction transaction =
                  (org.web3j.protocol.core.methods.response.Transaction) tx.get();

              transaction.setFrom(Keys.toChecksumAddress(transaction.getFrom()));

              if (transaction.getTo() != null && !transaction.getTo().isEmpty()) {
                transaction.setTo(Keys.toChecksumAddress(transaction.getTo()));
              }

              return new Web3jTransaction(transaction);
            })
        .collect(Collectors.toList());
  }
}
