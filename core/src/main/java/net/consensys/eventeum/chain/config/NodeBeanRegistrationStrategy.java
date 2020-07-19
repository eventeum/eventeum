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
import okhttp3.ConnectionPool;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
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
import org.web3j.utils.Async;

import javax.xml.bind.DatatypeConverter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private OkHttpClient globalOkHttpClient;

    public void register(Node node, BeanDefinitionRegistry registry) {
        registerContractEventDetailsFactoryBean(node, registry);

        final Web3jService web3jService = buildWeb3jService(node);

        final Web3j web3j = buildWeb3j(node, web3jService);

        final String blockchainServiceBeanName = registerBlockchainServiceBean(node, web3j, registry);

        final String blockSubStrategyBeanName = registerBlockSubscriptionStrategyBean(node, web3j, registry);

        registerNodeServicesBean(node, web3j, blockchainServiceBeanName, blockSubStrategyBeanName, registry);

        final String nodeFailureListenerBeanName =
                registerNodeFailureListener(node, blockSubStrategyBeanName, web3jService, registry);

        registerNodeHealthCheckBean(node, blockchainServiceBeanName,
                blockSubStrategyBeanName, web3jService, nodeFailureListenerBeanName, registry);
    }

    private String registerNodeServicesBean(Node node,
                                            Web3j web3j,
                                            String web3jServiceBeanName,
                                            String blockSubStrategyBeanName,
                                            BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                NodeServices.class);

        builder.addPropertyValue("nodeName", node.getName())
                .addPropertyValue("web3j", web3j)
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

    private String registerBlockchainServiceBean(Node node, Web3j web3j, BeanDefinitionRegistry registry) {

        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                net.consensys.eventeum.chain.service.Web3jService.class);

        builder.addConstructorArgValue(node.getName())
                .addConstructorArgValue(web3j)
                .addConstructorArgReference(String.format(CONTRACT_EVENT_DETAILS_FACTORY_BEAN_NAME, node.getName()));

        final String beanName = String.format(WEB3J_SERVICE_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

        return beanName;
    }

    private String registerNodeHealthCheckBean(Node node,
                                               String blockchainServiceBeanName,
                                               String blockSubStrategyBeanName,
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

        beanDefinition
                .getConstructorArgumentValues()
                .addIndexedArgumentValue(1, new RuntimeBeanReference(blockSubStrategyBeanName));


        final String beanName = String.format(NODE_FAILURE_LISTENER_BEAN_NAME, node.getName());
        registry.registerBeanDefinition(beanName, beanDefinition);

        return beanName;
    }

    private Web3jService buildWeb3jService(Node node) {
        Web3jService web3jService = null;

        Map<String, String> authHeaders;
        if (node.getUsername() != null && node.getPassword() != null) {
            authHeaders = new HashMap<>();
            authHeaders.put(
                    "Authorization",
                    "Basic " + DatatypeConverter.printBase64Binary(
                            String.format("%s:%s", node.getUsername(), node.getPassword()).getBytes()));
        } else {
            authHeaders = null;
        }

        if (isWebSocketUrl(node.getUrl())) {
            final URI uri = parseURI(node.getUrl());

            final WebSocketClient client = authHeaders != null ? new WebSocketClient(uri, authHeaders) : new WebSocketClient(uri);

            WebSocketService wsService = new EventeumWebSocketService(client, false);

            try {
                wsService.connect();
            } catch (ConnectException e) {
                throw new RuntimeException("Unable to connect to eth node websocket", e);
            }

            web3jService = wsService;
        } else {

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            ConnectionPool pool = new ConnectionPool(node.getMaxIdleConnections(), node.getKeepAliveDuration(), TimeUnit.MILLISECONDS);
            OkHttpClient client = globalOkHttpClient.newBuilder()
                    .connectionPool(pool)
                    . cookieJar(new JavaNetCookieJar(cookieManager))
                    .readTimeout(node.getReadTimeout(),TimeUnit.MILLISECONDS)
                    .connectTimeout(node.getConnectionTimeout(),TimeUnit.MILLISECONDS)
                    .build();
            HttpService httpService = new HttpService(node.getUrl(),client,false);
            if (authHeaders != null) {
                httpService.addHeaders(authHeaders);
            }
            web3jService = httpService;
        }

        return web3jService;
    }

    private Web3j buildWeb3j(Node node, Web3jService web3jService) {

        return Web3j.build(web3jService, node.getPollingInterval(), Async.defaultExecutorService());
    }

    private String registerBlockSubscriptionStrategyBean(Node node,
                                                         Web3j web3j,
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

        builder.addConstructorArgValue(web3j)
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
