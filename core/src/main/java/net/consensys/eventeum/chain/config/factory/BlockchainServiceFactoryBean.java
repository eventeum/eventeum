package net.consensys.eventeum.chain.config.factory;

import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.EventBlockManagementService;
import net.consensys.eventeum.chain.service.Web3jService;
import net.consensys.eventeum.chain.service.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.Node;
import org.springframework.beans.factory.FactoryBean;
import org.web3j.protocol.Web3j;

@Data
public class BlockchainServiceFactoryBean implements FactoryBean<BlockchainService> {

    private Node node;
    private Web3j web3j;
    private ContractEventDetailsFactory contractEventDetailsFactory;
    private EventBlockManagementService eventBlockManagementService;
    private BlockSubscriptionStrategy blockSubscriptionStrategy;

    @Override
    public BlockchainService getObject() throws Exception {
        return new Web3jService(node.getName(), web3j,
                contractEventDetailsFactory, eventBlockManagementService, blockSubscriptionStrategy);
    }

    @Override
    public Class<?> getObjectType() {
        return BlockchainService.class;
    }
}
