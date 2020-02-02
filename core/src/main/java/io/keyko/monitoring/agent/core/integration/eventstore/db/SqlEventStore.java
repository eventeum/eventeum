package io.keyko.monitoring.agent.core.integration.eventstore.db;

import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;
import io.keyko.monitoring.agent.core.integration.eventstore.SaveableEventStore;
import io.keyko.monitoring.agent.core.integration.eventstore.db.repository.ContractEventDetailsRepository;
import io.keyko.monitoring.agent.core.integration.eventstore.db.repository.LatestBlockRepository;
import io.keyko.monitoring.agent.core.model.LatestBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

/**
 * A saveable event store that stores contract events in a db repository.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class SqlEventStore implements SaveableEventStore {

    private ContractEventDetailsRepository eventDetailsRepository;

    private LatestBlockRepository latestBlockRepository;

    private JdbcTemplate jdbcTemplate;

    public SqlEventStore(
            ContractEventDetailsRepository eventDetailsRepository,
            LatestBlockRepository latestBlockRepository,
            JdbcTemplate jdbcTemplate) {
        this.eventDetailsRepository = eventDetailsRepository;
        this.latestBlockRepository = latestBlockRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<ContractEventDetails> getContractEventsForSignature(
            String eventSignature, String contractAddress, PageRequest pagination) {
        return eventDetailsRepository.findByEventSpecificationSignatureAndAddress(
                eventSignature, contractAddress, pagination);
    }

    @Override
    public Optional<LatestBlock> getLatestBlockForNode(String nodeName) {
        final Iterable<LatestBlock> blocks = latestBlockRepository.findAll();

        return latestBlockRepository.findById(nodeName);
    }

    @Override
    public boolean isPagingZeroIndexed() {
        return true;
    }

    @Override
    public void save(ContractEventDetails contractEventDetails) {
        eventDetailsRepository.save(contractEventDetails);
    }

    @Override
    public void save(LatestBlock latestBlock) {
        latestBlockRepository.save(latestBlock);
    }
}
