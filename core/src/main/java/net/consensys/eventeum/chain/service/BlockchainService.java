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

package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.contract.ContractEventListener;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.model.FilterSubscription;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Interface for a service that interacts directly with an Ethereum blockchain node.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface BlockchainService {

    /**
     *
     * @return The ethereum node name that this service is connected to.
     */
    String getNodeName();

    /**
     * Retrieves all events for a specified event filter.
     *
     * @param eventFilter The contract event filter that should be matched.
     * @param startBlock The start block
     * @param endBlock The end block
     * @return The blockchain contract events
     */
    List<ContractEventDetails> retrieveEvents(ContractEventFilter eventFilter,
                                              BigInteger startBlock,
                                              BigInteger endBlock);

    /**
     * Register a contract event listener for the specified event filter, that gets triggered when an event
     * matching the filter is emitted within the Ethereum network.
     *
     * @param eventFilter The contract event filter that should be matched.
     * @param eventListener The listener to be triggered when a matching event is emitted
     * @return The registered subscription
     */
    FilterSubscription registerEventListener(ContractEventFilter eventFilter,
                                             ContractEventListener eventListener,
                                             BigInteger startBlock,
                                             BigInteger endBlock,
                                             Optional<Runnable> onCompletion);

    /**
     *
     * @return the client version for the connected Ethereum node.
     */
    String getClientVersion();

    /**
     *
     * @return the current block number of the network that the Ethereum node is connected to.
     */
    BigInteger getCurrentBlockNumber();

    /**
     *
     * @param blockHash The hash of the block to obtain
     * @param fullTransactionObjects If full transaction details should be populated
     * @return The block for the specified hash or nothing if a block with the specified hash does not exist.
     */
    public Optional<Block> getBlock(String blockHash, boolean fullTransactionObjects);

    List<ContractEventDetails> getEventsForFilter(ContractEventFilter filter, BigInteger blockNumber);

    /**
     * Obtain the transaction receipt for a specified transaction id.
     *
     * @param txId the transaction id
     * @return the receipt for the transaction with the specified id.
     */
    TransactionReceipt getTransactionReceipt(String txId);



    String getRevertReason(String from, String to, BigInteger blockNumber, String input);
}
