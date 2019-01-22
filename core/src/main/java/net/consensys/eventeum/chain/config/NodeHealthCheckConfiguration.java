package net.consensys.eventeum.chain.config;

import net.consensys.eventeum.annotation.ConditionalOnWebsocket;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import net.consensys.eventeum.chain.service.health.WebSocketHealthCheckService;
import net.consensys.eventeum.chain.service.health.listener.NodeFailureListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.web3j.protocol.websocket.WebSocketClient;

import java.util.List;

//@Configuration
//@EnableScheduling
//public class NodeHealthCheckConfiguration {
//
//    @ConditionalOnWebsocket
//    @Configuration
//    public class WebSocketHeathCheckConfiguration {
//
//        public WebSocketHealthCheckService websocketHealthCheckService(BlockchainService blockchainService,
//                                                                       List<NodeFailureListener> failureListeners,
//                                                                       WebSocketClient webSocketClient) {
//            return new WebSocketHealthCheckService(blockchainService, failureListeners, webSocketClient);
//        }
//    }
//
//    @ConditionalOnWebsocket(false)
//    @Configuration
//    public class WebSocketHttpHeathCheckConfiguration {
//
//        public NodeHealthCheckService httpWebSocketHealthCheckService(BlockchainService blockchainService,
//                                                                      List<NodeFailureListener> failureListeners) {
//            return new NodeHealthCheckService(blockchainService, failureListeners);
//        }
//    }
//}
