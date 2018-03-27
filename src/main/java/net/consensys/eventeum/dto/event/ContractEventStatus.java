package net.consensys.eventeum.dto.event;

/**
 * The status of a contract event being broadcast from Eventeum.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public enum ContractEventStatus {
    //The transaction that triggered the event has been mined
    UNCONFIRMED,

    //The configured number of blocks since the event transaction has been reached
    //without a fork in the chain.
    CONFIRMED,

    //The chain has been forked and the event is no longer valid
    INVALIDATED;
}
