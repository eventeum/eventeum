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

import lombok.AllArgsConstructor;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;

@AllArgsConstructor
public class ListenerInvokingBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

  private OnBlockchainEventListener listener;

  @Override
  public void broadcastNewBlock(BlockDetails block) {
    listener.onNewBlock(block);
  }

  @Override
  public void broadcastContractEvent(ContractEventDetails eventDetails) {
    listener.onContractEvent(eventDetails);
  }

  @Override
  public void broadcastTransaction(TransactionDetails transactionDetails) {
    listener.onTransactionEvent(transactionDetails);
  }

  @Override
  public void broadcastMessage(MessageDetails messageDetails) {
    listener.onMessageEvent(messageDetails);
  }

  public interface OnBlockchainEventListener {

    void onNewBlock(BlockDetails block);

    void onContractEvent(ContractEventDetails eventDetails);

    void onTransactionEvent(TransactionDetails transactionDetails);

    void onMessageEvent(MessageDetails messageDetails);
  }
}
