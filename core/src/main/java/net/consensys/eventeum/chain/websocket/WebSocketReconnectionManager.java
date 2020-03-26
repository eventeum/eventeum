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

package net.consensys.eventeum.chain.websocket;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.web3j.protocol.websocket.WebSocketClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class WebSocketReconnectionManager {

    public synchronized void reconnect(WebSocketClient client) {
        log.info("Attempting websocket reconnection...");
        try {
            if (!client.reconnectBlocking()) {
                log.error("Reconnect failed!");
            } else {
                log.info("Websocket reconnected successfully.");
            }
        } catch (InterruptedException e) {
            log.error("Reconnect failed!", e);
        }
    }
}
