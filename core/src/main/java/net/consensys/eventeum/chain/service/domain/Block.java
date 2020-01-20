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

package net.consensys.eventeum.chain.service.domain;

import java.math.BigInteger;
import java.util.List;

public interface Block {

    BigInteger getNumber();
    String getHash();
    String getParentHash();
    BigInteger getNonce();
    String getSha3Uncles();
    String getLogsBloom();
    String getTransactionsRoot();
    String getStateRoot();
    String getReceiptsRoot();
    String getAuthor();
    String getMiner();
    String getMixHash();
    BigInteger getDifficulty();
    BigInteger getTotalDifficulty();
    String getExtraData();
    BigInteger getSize();
    BigInteger getGasLimit();
    BigInteger getGasUsed();
    BigInteger getTimestamp();
    List<Transaction> getTransactions();
    List<String> getUncles();
    List<String> getSealFields();

    //Eventeum specific
    String getNodeName();
}

