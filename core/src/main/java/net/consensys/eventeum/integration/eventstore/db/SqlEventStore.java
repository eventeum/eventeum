/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.integration.eventstore.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import net.consensys.eventeum.integration.eventstore.db.repository.LatestBlockRepository;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

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
