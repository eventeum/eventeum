package io.keyko.monitoring.agent.core.chain.config.factory;

import lombok.Data;
import io.keyko.monitoring.agent.core.chain.converter.EventParameterConverter;
import io.keyko.monitoring.agent.core.chain.factory.ContractEventDetailsFactory;
import io.keyko.monitoring.agent.core.chain.factory.DefaultContractEventDetailsFactory;
import io.keyko.monitoring.agent.core.chain.settings.Node;
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
