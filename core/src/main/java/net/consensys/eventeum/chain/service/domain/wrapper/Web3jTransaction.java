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

import lombok.Data;
import lombok.NoArgsConstructor;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.utils.ModelMapperFactory;
import org.modelmapper.ModelMapper;

@Data
@NoArgsConstructor
public class Web3jTransaction implements Transaction {

  private String hash;
  private String nonce;
  private String blockHash;
  private String blockNumber;
  private String transactionIndex;
  private String from;
  private String to;
  private String value;
  private String gasPrice;
  private String gas;
  private String input;
  private String creates;
  private String publicKey;
  private String raw;
  private String r;
  private String s;
  private long v;

  public Web3jTransaction(org.web3j.protocol.core.methods.response.Transaction web3jTransaction) {

    final ModelMapper modelMapper = ModelMapperFactory.getInstance().getModelMapper();
    modelMapper.map(web3jTransaction, this);
  }
}
