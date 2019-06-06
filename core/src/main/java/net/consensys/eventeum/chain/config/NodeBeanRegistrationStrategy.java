package net.consensys.eventeum.chain.config;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.config.factory.ContractEventDetailsFactoryFactoryBean;
import net.consensys.eventeum.chain.converter.Web3jEventParameterConverter;
import net.consensys.eventeum.chain.service.BlockchainException;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import net.consensys.eventeum.chain.service.health.WebSocketHealthCheckService;
import net.consensys.eventeum.chain.service.health.strategy.HttpReconnectionStrategy;
import net.consensys.eventeum.chain.service.health.strategy.WebSocketResubscribeNodeFailureListener;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.service.strategy.PollingBlockSubscriptionStrategy;
import net.consensys.eventeum.chain.service.strategy.PubSubBlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.settings.NodeSettings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.EventeumWebSocketService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

@AllArgsConstructor
public class NodeBeanRegistrationStrategy {

    private static final String WEB3J_SERVICE_BEAN_NAME = "%sWeb3jService";

    private static final String CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME =
            "%sContractEventDetailsFactory";

    private static final String NODE_SERVICES_BEAN_NAME =
            "%sNodeServices";

    private static final String NODE_HEALTH_CHECM_BEAN_NAME =
            "%sNodeHealthCheck";

    private static final String NODE_FAILURE_LISTENER_BEAN_NAME =
            "%sNodeFailureListener";

    private static final String WEB_SOCKET_CLIENT_BEAN_NAME = "%sWebSocketClient";

    private NodeSettings nodeSettings;

    public void register(Node node, BeanDefinitionRegistry registry) {
        registerContractEventDetailsFactoryBean(node, registry);

        final Web3jService web3jService = buildWeb3jService(node);
        final Web3j web3j = buildWeb3j(node, web3jService);
        final String blockchainServiceBeanName = registerBlockchainServiceBean(node, web3j, registry);
        registerNodeServicesBean(node, web3j, blockchainServiceBeanName, registry);
        final String nodeFailureListenerBeanName =
                registerNodeFailureListener(node, blockchainServiceBeanName, web3jService, registry);
        registerNodeHealthCheckBean(node, blockchainServiceBeanName, web3jService, nodeFailureListenerBeanName, registry);

    }

    private String registerNodeServicesBean(Node node,
                                            Web3j web3j,
                                            String web3jServiceBeanName,
                                            BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                NodeServices.class);

        builder.addPropertyValue("nodeName", node.getName())
                .addPropertyValue("web3j", web3j)
                .addPropertyReference("blockchainService", web3jServiceBeanName);

        final String beanName = String.format(NODE_SERVICES_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
    }

    private String registerContractEventDetailsFactoryBean(Node node, BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                ContractEventDetailsFactoryFactoryBean.class);

        builder.addPropertyValue("parameterConverter", new Web3jEventParameterConverter())
                .addPropertyReference("eventConfirmationConfig", "eventConfirmationConfig")
                .addPropertyValue("nodeName", node.getName());

        final String beanName = String.format(CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(String.format(CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME, node.getName()),
                builder.getBeanDefinition());

        return beanName;
    }

    private String registerBlockchainServiceBean(Node node, Web3j web3j, BeanDefinitionRegistry registry) {
        final BlockSubscriptionStrategy blockSubscriptionStrategy = buildBlockSubscriptionStrategy(node, web3j);

        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                net.consensys.eventeum.chain.service.Web3jService.class);

        builder.addConstructorArgValue(node.getName())
                .addConstructorArgValue(web3j)
                .addConstructorArgReference(String.format(CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME, node.getName()))
                .addConstructorArgReference("defaultEventBlockManagementService")
                .addConstructorArgValue(blockSubscriptionStrategy);

        final String beanName = String.format(WEB3J_SERVICE_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
    }

    private String registerNodeHealthCheckBean(Node node,
                                               String blockchainServiceBeanName,
                                               Web3jService web3jService,
                                               String nodeFailureListenerBeanName,
                                               BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder;

        if (isWebSocketUrl(node.getUrl())) {
            builder = BeanDefinitionBuilder.genericBeanDefinition(WebSocketHealthCheckService.class)
                    .addConstructorArgValue(web3jService);
        } else {
            builder = BeanDefinitionBuilder.genericBeanDefinition(NodeHealthCheckService.class);
        }

        builder.addConstructorArgReference(blockchainServiceBeanName);
        builder.addConstructorArgReference(nodeFailureListenerBeanName);

        final String beanName = String.format(NODE_HEALTH_CHECM_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
    }

    private String registerNodeFailureListener(Node node,
                                               String blockchainServiceBeanName,
                                               Web3jService web3jService,
                                               BeanDefinitionRegistry registry) {
        final BeanDefinition beanDefinition;

        if (isWebSocketUrl(node.getUrl())) {
            final EventeumWebSocketService webSocketService = (EventeumWebSocketService) web3jService;
            beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(WebSocketResubscribeNodeFailureListener.class)
                    .getBeanDefinition();

            beanDefinition.getConstructorArgumentValues()
                    .addIndexedArgumentValue(3, webSocketService.getWebSocketClient());

        } else {
            beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(HttpReconnectionStrategy.class)
                    .getBeanDefinition();
        }

        beanDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(1, new RuntimeBeanReference(blockchainServiceBeanName));


        final String beanName = String.format(NODE_FAILURE_LISTENER_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, beanDefinition);

        return beanName;
    }

    private Web3jService buildWeb3jService(Node node) {
        Web3jService web3jService = null;

        if (isWebSocketUrl(node.getUrl())) {
            final URI uri = parseURI(node.getUrl());

            final WebSocketClient client = new WebSocketClient(uri);

            WebSocketService wsService = new EventeumWebSocketService(client, false);

            try {
                wsService.connect();
            } catch (ConnectException e) {
                throw new RuntimeException("Unable to connect to eth node websocket", e);
            }

            web3jService = wsService;
        } else {
            web3jService = new HttpService(node.getUrl());
        }

        return web3jService;
    }

    private Web3j buildWeb3j(Node node, Web3jService web3jService) {

        return Web3j.build(web3jService);
    }

    private BlockSubscriptionStrategy buildBlockSubscriptionStrategy(Node node, Web3j web3j) {
        if (nodeSettings.getBlockStrategy().equals("POLL")) {
            return new PollingBlockSubscriptionStrategy(web3j, node.getName());
        } else if (nodeSettings.getBlockStrategy().equals("PUBSUB")) {
            return new PubSubBlockSubscriptionStrategy(web3j, node.getName());
        }

        throw new BlockchainException("Invalid blockstrategy configured");
    }

    private boolean isWebSocketUrl(String nodeUrl) {
        return nodeUrl.contains("wss://") || nodeUrl.contains("ws://");
    }

    private URI parseURI(String serverUrl) {
        try {
            return new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
        }
    }
}
