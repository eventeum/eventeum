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

package net.consensys.eventeum.service;

import java.util.List;
import java.util.concurrent.Future;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.service.exception.NotFoundException;

/**
 * A service for manageing contract event subscriptions within the Eventeum instance.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface SubscriptionService {

  /** Initialise the subscription service */
  void init(List<ContractEventFilter> initFilters);

  /**
   * Registers a new contract event filter.
   *
   * <p>If the id is null, then one is assigned.
   *
   * @param filter The filter to add.
   * @param broadcast Specifies if the added filter event should be broadcast to other Eventeum
   *     instances.
   * @return The registered contract event filter
   */
  ContractEventFilter registerContractEventFilter(ContractEventFilter filter, boolean broadcast);

  /**
   * Registers a new contract event filter.
   *
   * <p>If the id is null, then one is assigned.
   *
   * <p>Will retry indefinitely until successful
   *
   * @param filter The filter to add.
   * @param broadcast Specifies if the added filter event should be broadcast to other Eventeum
   *     instances.
   * @return The registered contract event filter
   */
  Future<ContractEventFilter> registerContractEventFilterWithRetries(
      ContractEventFilter filter, boolean broadcast);

  /**
   * List all registered contract event filters.
   *
   * @return The list of registered contract event filters
   */
  List<ContractEventFilter> listContractEventFilters();

  /**
   * Unregisters a previously added contract event filter.
   *
   * <p>Broadcasts the removed filter event to any other Eventeum instances.
   *
   * @param filterId The filter id of the event to remove.
   */
  void unregisterContractEventFilter(String filterId) throws NotFoundException;

  /**
   * Unregisters a previously added contract event filter.
   *
   * @param filterId The filter id of the event to remove.
   * @param broadcast Specifies if the removed filter event should be broadcast to other Eventeum
   *     instances.
   */
  void unregisterContractEventFilter(String filterId, boolean broadcast) throws NotFoundException;

  /** Unsubscribe all active listeners */
  void unsubscribeToAllSubscriptions(String nodeName);

  /**
   * @return the state of the service
   */
  SubscriptionServiceState getState();

  enum SubscriptionServiceState {
    UNINITIALISED,

    SYNCING_EVENTS,

    SUBSCRIBED
  }
}
