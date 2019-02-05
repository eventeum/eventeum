package net.consensys.eventeum.chain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Import(BlockchainServiceRegistrar.class)
public class BaseChainConfiguration {

    @Bean
    RetryTemplate foreverRetryTemplate() {
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
}
