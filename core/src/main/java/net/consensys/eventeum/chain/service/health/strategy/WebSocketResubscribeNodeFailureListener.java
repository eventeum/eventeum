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
import net.consensys.eventeum.chain.web3j.Web3jContainer;
import net.consensys.eventeum.chain.websocket.WebSocketReconnectionManager;
import net.consensys.eventeum.service.SubscriptionService;
import org.web3j.protocol.websocket.EventeumWebSocketService;

import java.io.IOException;

/**
 * An NodeFailureListener that reconnects to the websocket server on failure, and
 * reconnects the block subscription and resubscribes to all
 * active event subscriptions on recovery.
 *
 * Note:  All subscriptions are unregistered before being reregistered.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class WebSocketResubscribeNodeFailureListener extends ResubscribingReconnectionStrategy {

    private WebSocketReconnectionManager reconnectionManager;
    private Web3jContainer web3jContainer;

    public WebSocketResubscribeNodeFailureListener(SubscriptionService subscriptionService,
                                                   BlockSubscriptionStrategy blockSubscription,
                                                   WebSocketReconnectionManager reconnectionManager,
                                                   Web3jContainer web3jContainer) {
        super(subscriptionService, blockSubscription);

        this.reconnectionManager = reconnectionManager;
        this.web3jContainer = web3jContainer;
    }

    @Override
    public void reconnect() {
        log.info("Reconnecting web socket because of {} node failure", getBlockSubscriptionStrategy().getNodeName());

        final EventeumWebSocketService web3jService = getEventeumWebSocketService();
        reconnectionManager.reconnect(web3jService.getWebSocketClient());
    }

    @Override
    public void onError() {
        log.warn("Reinitialising web3j");

        try {
            web3jContainer.getWeb3jService().close();
        } catch (IOException e) {
            log.warn("Failed to close websocker", e);
        }

        web3jContainer.reinitialise();
    }

    private EventeumWebSocketService getEventeumWebSocketService() {
        return (EventeumWebSocketService) web3jContainer.getWeb3jService();
    }
}
