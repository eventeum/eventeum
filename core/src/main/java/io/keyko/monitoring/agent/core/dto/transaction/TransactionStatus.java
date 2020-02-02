package io.keyko.monitoring.agent.core.dto.transaction;

public enum TransactionStatus {

    //The transaction has been mined
    UNCONFIRMED,

    //The configured number of blocks since the event transaction has been reached
    //without a fork in the chain.
    CONFIRMED,

    //The chain has been forked and the transaction is no longer valid
    INVALIDATED,

    //The transaction failed
    FAILED;
}
