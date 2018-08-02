package net.consensys.eventeum.chain.config;

import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.service.strategy.PollingBlockSubscriptionStrategy;
import net.consensys.eventeum.chain.service.strategy.PubSubBlockSubscriptionStrategy;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;

@Configuration
public class BlockSubscriptionStrategyConfiguration {

    @Bean
    @ConditionalOnProperty(name="ethereum.blockStrategy", havingValue="POLL")
    BlockSubscriptionStrategy pollingStrategy(Web3j web3j, AsyncTaskService asyncTaskService) {
        return new PollingBlockSubscriptionStrategy(web3j, asyncTaskService);
    }

    @Bean
    @ConditionalOnProperty(name="ethereum.blockStrategy", havingValue="PUBSUB")
    BlockSubscriptionStrategy pubsubStrategy(Web3j web3j, AsyncTaskService asyncTaskService) {
        return new PubSubBlockSubscriptionStrategy(web3j, asyncTaskService);
    }
}
