package net.consensys.eventeum.chain.config.factory;

import lombok.Data;
import net.consensys.eventeum.chain.converter.EventParameterConverter;
import net.consensys.eventeum.chain.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.factory.DefaultContractEventDetailsFactory;
import net.consensys.eventeum.chain.settings.Node;
import org.springframework.beans.factory.FactoryBean;
import org.web3j.abi.datatypes.Type;

@Data
public class ContractEventDetailsFactoryFactoryBean
        implements FactoryBean<ContractEventDetailsFactory> {

    EventParameterConverter<Type> parameterConverter;
    Node node;
    String nodeName;

    @Override
    public ContractEventDetailsFactory getObject() throws Exception {
        return new DefaultContractEventDetailsFactory(
                parameterConverter, node, nodeName);
    }

    @Override
    public Class<?> getObjectType() {
        return ContractEventDetailsFactory.class;
    }
}
