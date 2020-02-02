package io.keyko.monitoring.agent.core.integration.eventstore;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

/**
 * Interface for integrating with an event store, in order to obtain events for a specified signature.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventStore {
    Page<ContractEventDetails> getContractEventsForSignature(
            String eventSignature, String contractAddress, PageRequest pagination);

    Optional<LatestBlock> getLatestBlockForNode(String nodeName);

    boolean isPagingZeroIndexed();
}
