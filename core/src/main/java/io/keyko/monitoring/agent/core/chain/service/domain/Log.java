package io.keyko.monitoring.agent.core.chain.service.domain;

import java.math.BigInteger;
import java.util.List;

/**
 * An Ethereum log entry.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface Log {

    boolean isRemoved();

    BigInteger getLogIndex();

    BigInteger getTransactionIndex();

    String getTransactionHash();

    String getBlockHash();

    BigInteger getBlockNumber();

    String getAddress();

    String getData();

    String getType();

    List<String> getTopics();
}
