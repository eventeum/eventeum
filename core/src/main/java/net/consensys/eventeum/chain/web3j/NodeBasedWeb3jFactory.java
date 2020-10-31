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

package net.consensys.eventeum.chain.web3j;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.settings.Node;
import okhttp3.ConnectionPool;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.EventeumWebSocketService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Async;

import javax.xml.bind.DatatypeConverter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class NodeBasedWeb3jFactory implements Web3jFactory {

    private Node node;

    private OkHttpClient baseClient;

    @Override
    public Web3jAndService build() {

        final Web3jService web3jService = buildWeb3jService(node);
        final Web3j web3j = buildWeb3j(node, web3jService);

        return new Web3jAndService(web3j, web3jService);
    }

    private Web3j buildWeb3j(Node node, Web3jService web3jService) {

        return Web3j.build(web3jService, node.getPollingInterval(), Async.defaultExecutorService());
    }

    private Web3jService buildWeb3jService(Node node) {
        Web3jService web3jService = null;

        Map<String, String> authHeaders;
        if (node.getUsername() != null && node.getPassword() != null) {
            authHeaders = new HashMap<>();
            authHeaders.put(
                    "Authorization",
                    "Basic " + DatatypeConverter.printBase64Binary(
                            String.format("%s:%s", node.getUsername(), node.getPassword()).getBytes()));
        } else {
            authHeaders = null;
        }

        if (isWebSocketUrl(node.getUrl())) {
            final URI uri = parseURI(node.getUrl());

            final WebSocketClient client = authHeaders != null ? new WebSocketClient(uri, authHeaders) : new WebSocketClient(uri);

            WebSocketService wsService = new EventeumWebSocketService(client, false);

            try {
                wsService.connect();
            } catch (ConnectException e) {
                throw new RuntimeException("Unable to connect to eth node websocket", e);
            }

            web3jService = wsService;
        } else {

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            ConnectionPool pool = new ConnectionPool(node.getMaxIdleConnections(), node.getKeepAliveDuration(), TimeUnit.MILLISECONDS);
            OkHttpClient client = baseClient.newBuilder()
                    .connectionPool(pool)
                    . cookieJar(new JavaNetCookieJar(cookieManager))
                    .readTimeout(node.getReadTimeout(),TimeUnit.MILLISECONDS)
                    .connectTimeout(node.getConnectionTimeout(),TimeUnit.MILLISECONDS)
                    .build();
            HttpService httpService = new HttpService(node.getUrl(),client,false);
            if (authHeaders != null) {
                httpService.addHeaders(authHeaders);
            }
            web3jService = httpService;
        }

        return web3jService;
    }

    private boolean isWebSocketUrl(String nodeUrl) {
        return nodeUrl.contains("wss://") || nodeUrl.contains("ws://");
    }

    private URI parseURI(String serverUrl) {
        try {
            return new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
        }
    }
}
