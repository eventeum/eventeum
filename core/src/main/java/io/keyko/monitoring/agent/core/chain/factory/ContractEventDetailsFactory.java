package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
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
