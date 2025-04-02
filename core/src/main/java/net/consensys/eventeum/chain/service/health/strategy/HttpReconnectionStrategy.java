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

package net.consensys.eventeum.chain.service.health.strategy;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.service.SubscriptionService;

/**
 * An NodeFailureListener that reconnects the blockchain service and resubscribes to all active
 * event subscriptions on recovery.
 *
 * <p>Note: All subscriptions are unregistered before being reregistered.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class HttpReconnectionStrategy extends ResubscribingReconnectionStrategy {

  public HttpReconnectionStrategy(
      SubscriptionService subscriptionService, BlockSubscriptionStrategy blockSubscription) {
    super(subscriptionService, blockSubscription);
  }

  @Override
  public void reconnect() {
    // Do Nothing
  }
}
