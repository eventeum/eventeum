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

package net.consensys.eventeum.integration.broadcast.blockchain;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;

/**
 * An interface for a class that broadcasts ethereum blockchain details to the wider system.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface BlockchainEventBroadcaster {

  /**
   * Broadcast details of a new block that has been mined.
   *
   * @param block
   */
  void broadcastNewBlock(BlockDetails block);

  /**
   * Broadcasts details of a new smart contract event that has been emitted from the blockchain.
   *
   * @param eventDetails
   */
  void broadcastContractEvent(ContractEventDetails eventDetails);

  /**
   * Broadcasts details of a monitored transaction that has been mined.
   *
   * @param transactionDetails
   */
  void broadcastTransaction(TransactionDetails transactionDetails);

  /**
   * Broadcasts details of a message that has been emitted from the blockchain
   *
   * @param messageDetails
   */
  void broadcastMessage(MessageDetails messageDetails);
}
