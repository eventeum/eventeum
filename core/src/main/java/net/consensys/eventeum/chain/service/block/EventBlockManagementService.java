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

import java.math.BigInteger;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;

/**
 * Interface for a service that manages the latest block that has been seen to a specific event
 * specification.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventBlockManagementService {

  /**
   * Update the latest block number state for an event specification.
   *
   * @param eventSpecHash The event specification hash.
   * @param blockNumber The new latest block number.
   * @param address The address of the contract.
   */
  void updateLatestBlock(String eventSpecHash, BigInteger blockNumber, String address);

  /**
   * Retrieve the latest block number that has been seen for a specified event specification.
   *
   * @param eventFilter The event filter.
   * @return The latest block number that has been seen for a specified event specification.
   */
  BigInteger getLatestBlockForEvent(ContractEventFilter eventFilter);
}
