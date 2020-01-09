package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.web3j.protocol.core.methods.response.Log;

/**
 * A factory interface for creating ContractEventDetails objects from the event filter plus the
 * Web3J log.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface ContractEventDetailsFactory {
    ContractEventDetails createEventDetails(ContractEventFilter eventFilter, Log log);
}
