package net.consensys.eventeum.chain.config;

import net.consensys.eventeum.chain.web3j.RetryableWebSocketClient;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
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
    Web3j web3j(@Value("${ethereum.node.url}") String url, AsyncTaskService asyncTaskService) {
        final URI uri = parseURI(url);
        Web3jService service;

        if (uri.getScheme().startsWith("ws")) {
            try {
                final WebSocketClient client =
                        new RetryableWebSocketClient(uri, websocketRetryTemplate(), asyncTaskService);
                WebSocketService wsService = new WebSocketService(client, false);
                wsService.connect();
                service = wsService;
            } catch (ConnectException e) {
                throw new RuntimeException("Unable to connect to eth node websocket", e);
            }
        } else {
            service = new HttpService(url);
        }

        return Web3j.build(service);
    }

    @Bean
    public RetryTemplate websocketRetryTemplate() {
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

    private static URI parseURI(String serverUrl) {
        try {
            return new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
        }
    }
}
