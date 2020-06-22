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

package net.consensys.eventeum.service.catchup;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class BatchedEventRetriever implements EventRetriever {

    private static final BigInteger BATCH_SIZE = BigInteger.valueOf(10000L);

    private ChainServicesContainer servicesContainer;

    @Override
    public void retrieveEvents(ContractEventFilter eventFilter,
                                                     BigInteger startBlock,
                                                     BigInteger endBlock,
                                                     Consumer<List<ContractEventDetails>> eventConsumer) {

        BigInteger batchStartBlock = startBlock;

        while (batchStartBlock.compareTo(endBlock) < 0) {
            BigInteger batchEndBlock;

            if (batchStartBlock.add(BATCH_SIZE).compareTo(endBlock) >= 0) {
                batchEndBlock = endBlock;
            } else {
                batchEndBlock = batchStartBlock.add(BATCH_SIZE);
            }

            final List<ContractEventDetails> events = servicesContainer
                    .getNodeServices(eventFilter.getNode())
                    .getBlockchainService()
                    .retrieveEvents(eventFilter, batchStartBlock, batchEndBlock);

            eventConsumer.accept(events);

            batchStartBlock = batchEndBlock.add(BigInteger.ONE);
        }
    }
}
