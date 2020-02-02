package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.settings.Node;

public interface BlockchainServiceFactory {

    BlockchainService create(Node node);
}
