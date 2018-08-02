package net.consensys.eventeum.chain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
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
    Web3j web3j(@Value("${ethereum.node.url}") String url) {
        final URI uri = parseURI(url);
        Web3jService service;

        if (uri.getScheme().startsWith("ws")) {
            try {
                WebSocketService wsService = new WebSocketService(url, false);
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

    private static URI parseURI(String serverUrl) {
        try {
            return new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
        }
    }
}
