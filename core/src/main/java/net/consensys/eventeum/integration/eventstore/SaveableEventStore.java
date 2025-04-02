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

package net.consensys.eventeum.integration.eventstore;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.message.MessageDetails;
import net.consensys.eventeum.model.LatestBlock;

/**
 * Interface for integrating with an event store that supports direct saving of events.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface SaveableEventStore extends EventStore {
  void save(ContractEventDetails contractEventDetails);

  void save(LatestBlock latestBlock);

  void save(MessageDetails messageDetails);
}
