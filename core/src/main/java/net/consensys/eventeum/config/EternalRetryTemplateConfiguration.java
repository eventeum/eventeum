package net.consensys.eventeum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class EternalRetryTemplateConfiguration {

    private static final  long  DEFAULT_BACKOFF_TIME = 500l;

    @Bean
    public RetryTemplate eternalRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(DEFAULT_BACKOFF_TIME);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        AlwaysRetryPolicy retryPolicy = new AlwaysRetryPolicy();
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
