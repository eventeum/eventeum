package net.consensys.eventeum.chain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

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
        return Web3j.build(new HttpService(url));
    }
}
