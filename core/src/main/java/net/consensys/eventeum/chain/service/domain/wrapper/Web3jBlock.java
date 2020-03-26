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
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.utils.ModelMapperFactory;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Data
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
        final ModelMapper modelMapper = ModelMapperFactory.getInstance().createModelMapper();
        modelMapper.typeMap(
                EthBlock.Block.class, Web3jBlock.class)
                .addMappings(mapper -> {
                    mapper.skip(Web3jBlock::setTransactions);

                    //Nonce can be null which throws exception in web3j when
                    //calling getNonce (because of attempted hex conversion)
                    if (web3jBlock.getNonceRaw() == null) {
                        mapper.skip(Web3jBlock::setNonce);
                    }
                });

        modelMapper.map(web3jBlock, this);

        transactions = convertTransactions(web3jBlock.getTransactions());

        this.nodeName = nodeName;
    }

    private List<Transaction> convertTransactions(List<EthBlock.TransactionResult> toConvert) {
        return toConvert.stream()
                .map(tx -> {
                    org.web3j.protocol.core.methods.response.Transaction transaction = (org.web3j.protocol.core.methods.response.Transaction) tx.get();

                    transaction.setFrom(Keys.toChecksumAddress(transaction.getFrom()));

                    if (transaction.getTo() != null && !transaction.getTo().isEmpty()) {
                        transaction.setTo(Keys.toChecksumAddress(transaction.getTo()));
                    }

                    return new Web3jTransaction(transaction);
                })
                .collect(Collectors.toList());
    }
}
