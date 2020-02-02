package io.keyko.monitoring.agent.core.chain.service.domain;

import java.math.BigInteger;
import java.util.List;

/**
 * An Ethereum transaction receipt.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface TransactionReceipt {

    String getTransactionHash();

    BigInteger getTransactionIndex();

    String getBlockHash();

    BigInteger getBlockNumber();

    BigInteger getCumulativeGasUsed();

    BigInteger getGasUsed();

    String getContractAddress();

    String getRoot();

    String getFrom();

    String getTo();

    List<Log> getLogs();

    String getLogsBloom();

    String getStatus();
}
