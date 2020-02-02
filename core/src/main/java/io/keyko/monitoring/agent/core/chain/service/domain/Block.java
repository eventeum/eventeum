package io.keyko.monitoring.agent.core.chain.service.domain;

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

