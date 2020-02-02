package io.keyko.monitoring.agent.core.chain.service.domain;

public interface Transaction {

    String getHash();

    String getNonce();

    String getBlockHash();

    String getBlockNumber();

    String getTransactionIndex();

    String getFrom();

    String getTo();

    String getValue();

    String getGasPrice();

    String getGas();

    String getInput();

    String getCreates();

    String getPublicKey();

    String getR();

    String getS();

    long getV();
}
