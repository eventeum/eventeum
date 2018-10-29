package net.consensys.eventeum.chain.config;

import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.NodeFailureListener;
import net.consensys.eventeum.chain.service.ResubscribeNodeFailureListener;
import net.consensys.eventeum.chain.websocket.RetryableWebSocketClient;
import net.consensys.eventeum.chain.websocket.WebSocketReconnectionManager;
import net.consensys.eventeum.chain.websocket.WebSocketResubscribeNodeFailureListener;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuration for the Web3J instance
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
@EnableScheduling
public class Web3jConfiguration {

    @Bean
    Web3j web3j(Web3jService service) {

        return Web3j.build(service);
    }

    @ConditionalOnExpression("'${ethereum.node.url}'.contains('wss://') || '${ethereum.node.url}'.contains('ws://')")
    @Configuration
    public class WebSocketConfiguration {

        @Bean
        WebSocketClient webSocketClient(@Value("${ethereum.node.url}") String url,
                                        AsyncTaskService asyncTaskService,
                                        WebSocketReconnectionManager reconnectionManager) {
            final URI uri = parseURI(url);

            return new RetryableWebSocketClient(uri, reconnectionManager);
        }

        @Bean
        WebSocketService webSocketService(WebSocketClient client) {
            WebSocketService wsService = new WebSocketService(client, false);

            try {
                wsService.connect();
            } catch (ConnectException e) {
                throw new RuntimeException("Unable to connect to eth node websocket", e);
            }

            return wsService;
        }

        @Bean
        RetryTemplate websocketRetryTemplate() {
            final RetryTemplate retryTemplate = new RetryTemplate();

            final FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
            fixedBackOffPolicy.setBackOffPeriod(2000l);
            retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

            //AlwaysRetryPolicy seems to ignore backoff policy
            final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicy.setMaxAttempts(Integer.MAX_VALUE);
            retryTemplate.setRetryPolicy(retryPolicy);

//        final AlwaysRetryPolicy retryPolicy = new AlwaysRetryPolicy();
//        retryTemplate.setRetryPolicy(retryPolicy);

            return retryTemplate;
        }

        @Bean
        WebSocketReconnectionManager reconnectionManager(AsyncTaskService asyncTaskService) {
            return new WebSocketReconnectionManager(websocketRetryTemplate(), asyncTaskService);
        }

        @Bean
        WebSocketResubscribeNodeFailureListener websocketFailureListener(SubscriptionService subscriptionService,
                                                                         BlockchainService blockchainService,
                                                                         WebSocketReconnectionManager reconnectionManager,
                                                                         WebSocketClient client) {
            return new WebSocketResubscribeNodeFailureListener(subscriptionService,
                    blockchainService, reconnectionManager, client);
        }

        private URI parseURI(String serverUrl) {
            try {
                return new URI(serverUrl);
            } catch (URISyntaxException e) {
                throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
            }
        }
    }

    @ConditionalOnExpression("!('${ethereum.node.url}'.contains('wss://') || '${ethereum.node.url}'.contains('ws://'))")
    @Configuration
    public class HttpConfiguration {

        @Bean
        HttpService webSocketService(@Value("${ethereum.node.url}") String url) {
            return new HttpService(url);
        }

        @Bean
        NodeFailureListener resubscribeNodeFailureListener(SubscriptionService subscriptionService,
                                                           BlockchainService blockchainService) {
            return new ResubscribeNodeFailureListener(subscriptionService, blockchainService);
        }
    }
}
