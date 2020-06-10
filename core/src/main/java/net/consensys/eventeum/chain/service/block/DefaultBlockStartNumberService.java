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

package net.consensys.eventeum.chain.service.block;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.service.EventStoreService;
import net.consensys.eventeum.settings.EventeumSettings;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DefaultBlockStartNumberService implements BlockStartNumberService {

    private EventeumSettings settings;

    private EventStoreService eventStoreService;

    @Override
    public Optional<BigInteger> getStartBlockForNode(String nodeName) {
        final Optional<LatestBlock> latestBlock = getLatestBlock(nodeName);

        if (latestBlock.isPresent()) {
            final BigInteger latestBlockNumber = latestBlock.get().getNumber();

            final BigInteger startBlock = latestBlockNumber.subtract(settings.getNumBlocksToReplay());

            //Check the replay subtraction result is positive
            return Optional.of(startBlock.signum() == 1 ? startBlock : BigInteger.ONE);
        }

        return Optional.ofNullable(settings.getInitialStartBlock());
    }

    protected Optional<LatestBlock> getLatestBlock(String nodeName) {
        return eventStoreService.getLatestBlock(nodeName);
    }
}
