package net.consensys.eventeum.chain.service.domain.wrapper;

import lombok.Data;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class Web3jBlock implements Block {

    private String number;
    private String hash;
    private String parentHash;
    private String nonce;
    private String sha3Uncles;
    private String logsBloom;
    private String transactionsRoot;
    private String stateRoot;
    private String receiptsRoot;
    private String author;
    private String miner;
    private String mixHash;
    private String difficulty;
    private String totalDifficulty;
    private String extraData;
    private String size;
    private String gasLimit;
    private String gasUsed;
    private String timestamp;
    private List<Transaction> transactions;
    private List<String> uncles;
    private List<String> sealFields;

    public Web3jBlock(EthBlock.Block web3jBlock) {
        transactions = convertTransactions(web3jBlock.getTransactions());
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
