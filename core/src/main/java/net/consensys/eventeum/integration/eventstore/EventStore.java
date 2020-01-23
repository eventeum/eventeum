package net.consensys.eventeum.integration.eventstore;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.model.LatestBlock;
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
