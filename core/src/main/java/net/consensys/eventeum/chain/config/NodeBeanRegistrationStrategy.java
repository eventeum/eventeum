/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.chain.config;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.chain.config.factory.ContractEventDetailsFactoryFactoryBean;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import net.consensys.eventeum.chain.service.health.WebSocketHealthCheckService;
import net.consensys.eventeum.chain.service.health.strategy.HttpReconnectionStrategy;
import net.consensys.eventeum.chain.service.health.strategy.WebSocketResubscribeNodeFailureListener;
import net.consensys.eventeum.chain.service.strategy.PollingBlockSubscriptionStrategy;
import net.consensys.eventeum.chain.service.strategy.PubSubBlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.settings.NodeSettings;
import net.consensys.eventeum.chain.web3j.NodeBasedWeb3jFactory;
import net.consensys.eventeum.chain.web3j.Web3jContainer;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.net.URI;
import java.net.URISyntaxException;

@AllArgsConstructor
public class NodeBeanRegistrationStrategy {

    private static final String WEB3J_SERVICE_BEAN_NAME = "%sWeb3jService";

    private static final String CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME =
            "%sContractEventDetailsFactory";

    private static final String NODE_SERVICES_BEAN_NAME =
            "%sNodeServices";

    private static final String NODE_HEALTH_CHECK_BEAN_NAME =
            "%sNodeHealthCheck";

    private static final String NODE_FAILURE_LISTENER_BEAN_NAME =
            "%sNodeFailureListener";

    private static final String NODE_BLOCK_SUB_STRATEGY_BEAN_NAME =
            "%sBlockSubscriptionStategy";

    private static final String WEB_SOCKET_CLIENT_BEAN_NAME = "%sWebSocketClient";

    private NodeSettings nodeSettings;
    private OkHttpClient baseHttpClient;

    public void register(Node node, BeanDefinitionRegistry registry) {
        registerContractEventDetailsFactoryBean(node, registry);

        final Web3jContainer web3jContainer = buildWeb3jContainer(node);

        final String blockchainServiceBeanName = registerBlockchainServiceBean(node, web3jContainer, registry);

        final String blockSubStrategyBeanName = registerBlockSubscriptionStrategyBean(node, web3jContainer, registry);

        registerNodeServicesBean(node, web3jContainer, blockchainServiceBeanName, blockSubStrategyBeanName, registry);

        final String nodeFailureListenerBeanName =
                registerNodeFailureListener(node, blockSubStrategyBeanName, web3jContainer, registry);

        registerNodeHealthCheckBean(node, blockchainServiceBeanName,
                blockSubStrategyBeanName, web3jContainer, nodeFailureListenerBeanName, registry);
    }

    private String registerNodeServicesBean(Node node,
                                            Web3jContainer web3jContainer,
                                            String web3jServiceBeanName,
                                            String blockSubStrategyBeanName,
                                            BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                NodeServices.class);

        builder.addPropertyValue("nodeName", node.getName())
                .addPropertyValue("web3jContainer", web3jContainer)
                .addPropertyReference("blockchainService", web3jServiceBeanName)
                .addPropertyReference("blockSubscriptionStrategy", blockSubStrategyBeanName);

        final String beanName = String.format(NODE_SERVICES_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
    }

    private String registerContractEventDetailsFactoryBean(Node node, BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                ContractEventDetailsFactoryFactoryBean.class);

        builder.addPropertyReference("parameterConverter", "web3jEventParameterConverter")
                .addPropertyValue("node", node)
                .addPropertyValue("nodeName", node.getName());

        final String beanName = String.format(CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(String.format(CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME, node.getName()),
                builder.getBeanDefinition());

        return beanName;
    }

    private String registerBlockchainServiceBean(Node node,
                                                 Web3jContainer web3jContainer,
                                                 BeanDefinitionRegistry registry) {

        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                net.consensys.eventeum.chain.service.Web3jService.class);

        builder.addConstructorArgValue(node.getName())
                .addConstructorArgValue(web3jContainer)
                .addConstructorArgReference(String.format(CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME, node.getName()));

        final String beanName = String.format(WEB3J_SERVICE_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
    }

    private String registerNodeHealthCheckBean(Node node,
                                               String blockchainServiceBeanName,
                                               String blockSubStrategyBeanName,
                                               Web3jContainer web3jContainer,
                                               String nodeFailureListenerBeanName,
                                               BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder;

        if (isWebSocketUrl(node.getUrl())) {
            builder = BeanDefinitionBuilder.genericBeanDefinition(WebSocketHealthCheckService.class)
                    .addConstructorArgValue(web3jContainer);
        } else {
            builder = BeanDefinitionBuilder.genericBeanDefinition(NodeHealthCheckService.class);
        }

        builder.addConstructorArgReference(blockchainServiceBeanName);
        builder.addConstructorArgReference(blockSubStrategyBeanName);
        builder.addConstructorArgReference(nodeFailureListenerBeanName);
        builder.addConstructorArgReference("defaultSubscriptionService");
        builder.addConstructorArgReference("eventeumValueMonitor");
        builder.addConstructorArgReference("defaultEventStoreService");
        builder.addConstructorArgValue(node.getSyncingThreshold());
        builder.addConstructorArgReference("taskScheduler");
        builder.addConstructorArgValue(node.getHealthcheckInterval());

        final String beanName = String.format(NODE_HEALTH_CHECK_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
    }

    private String registerNodeFailureListener(Node node,
                                               String blockSubStrategyBeanName,
                                               Web3jContainer web3jContainer,
                                               BeanDefinitionRegistry registry) {
        final BeanDefinition beanDefinition;

        if (isWebSocketUrl(node.getUrl())) {
            beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(WebSocketResubscribeNodeFailureListener.class)
                    .getBeanDefinition();

            beanDefinition.getConstructorArgumentValues()
                    .addIndexedArgumentValue(3, web3jContainer);

        } else {
            beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(HttpReconnectionStrategy.class)
                    .getBeanDefinition();
        }

        beanDefinition
                .getConstructorArgumentValues()
                .addIndexedArgumentValue(1, new RuntimeBeanReference(blockSubStrategyBeanName));


        final String beanName = String.format(NODE_FAILURE_LISTENER_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, beanDefinition);

        return beanName;
    }

    private Web3jContainer buildWeb3jContainer(Node node) {
        return new Web3jContainer(new NodeBasedWeb3jFactory(node, baseHttpClient));
    }

    private String registerBlockSubscriptionStrategyBean(Node node,
                                                         Web3jContainer web3jContainer,
                                                         BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = null;

        String nodeBlockStrategy = node.getBlockStrategy();

        if (nodeBlockStrategy != null) {
            if (nodeBlockStrategy.equals("POLL")) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(PollingBlockSubscriptionStrategy.class);
            } else if (nodeBlockStrategy.equals("PUBSUB")) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(PubSubBlockSubscriptionStrategy.class);
            }
        } else {
            if (nodeSettings.getBlockStrategy().equals("POLL")) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(PollingBlockSubscriptionStrategy.class);
            } else if (nodeSettings.getBlockStrategy().equals("PUBSUB")) {
                builder = BeanDefinitionBuilder.genericBeanDefinition(PubSubBlockSubscriptionStrategy.class);
            }
        }

        builder.addConstructorArgValue(web3jContainer)
                .addConstructorArgValue(node.getName())
                .addConstructorArgReference("asyncTaskService")
                .addConstructorArgReference("defaultBlockNumberService");

        final String beanName = String.format(NODE_BLOCK_SUB_STRATEGY_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
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
