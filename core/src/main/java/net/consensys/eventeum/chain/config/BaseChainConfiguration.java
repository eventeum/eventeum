package net.consensys.eventeum.chain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Import(BlockchainServiceRegistrar.class)
public class BaseChainConfiguration {
}
