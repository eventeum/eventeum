package net.consensys.eventeum.chain.service.domain;

import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.List;

public interface Block {

    String getNumber();
    String getHash();
    String getParentHash();
    String getNonce();
    String getSha3Uncles();
    String getLogsBloom();
    String getTransactionsRoot();
    String getStateRoot();
    String getReceiptsRoot();
    String getAuthor();
    String getMiner();
    String getMixHash();
    String getDifficulty();
    String getTotalDifficulty();
    String getExtraData();
    String getSize();
    String getGasLimit();
    String getGasUsed();
    String getTimestamp();
    List<Transaction> getTransactions();
    List<String> getUncles();
    List<String> getSealFields();
}

