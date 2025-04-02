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

package org.web3j.protocol.websocket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Web3j seems to fail when closing websocket in pubsub mode. This causes a deadlock when attempting
 * to reconnect to the node, as the close latch in WebSocketClient never gets released and the
 * reconnection block indefinitely.
 *
 * <p>This is workaround until web3j 4 which should hopefully have built in reconnections.
 */
@Slf4j
@Getter
public class EventeumWebSocketService extends WebSocketService {

  private final WebSocketClient webSocketClient;

  public EventeumWebSocketService(WebSocketClient webSocketClient, boolean includeRawResponses) {
    super(webSocketClient, includeRawResponses);

    this.webSocketClient = webSocketClient;
  }

  @Override
  void onWebSocketClose() {
    try {
      super.onWebSocketClose();
    } catch (Throwable t) {
      log.warn(
          "Error when closing websocket, this is expected during a websocket reconnection (for now).",
          t);
    }
  }
}
