package io.keyko.monitoring.agent.core.chain.config.factory;

import io.keyko.monitoring.agent.core.chain.service.strategy.BlockSubscriptionStrategy;
import lombok.Data;
import io.keyko.monitoring.agent.core.chain.factory.ContractEventDetailsFactory;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import io.keyko.monitoring.agent.core.chain.service.EventBlockManagementService;
import io.keyko.monitoring.agent.core.chain.service.Web3jService;
import io.keyko.monitoring.agent.core.chain.settings.Node;
import io.keyko.monitoring.agent.core.service.AsyncTaskService;
import org.springframework.beans.factory.FactoryBean;
import org.web3j.protocol.Web3j;

@Data
public class BlockchainServiceFactoryBean implements FactoryBean<BlockchainService> {

    private Node node;
    private Web3j web3j;
    private ContractEventDetailsFactory contractEventDetailsFactory;
    private EventBlockManagementService eventBlockManagementService;
    private BlockSubscriptionStrategy blockSubscriptionStrategy;
    private AsyncTaskService asyncTaskService;

    @Override
    public BlockchainService getObject() throws Exception {
        return new Web3jService(node.getName(), web3j,
                contractEventDetailsFactory, eventBlockManagementService, blockSubscriptionStrategy, asyncTaskService);
    }

    @Override
    public Class<?> getObjectType() {
        return BlockchainService.class;
    }
}
