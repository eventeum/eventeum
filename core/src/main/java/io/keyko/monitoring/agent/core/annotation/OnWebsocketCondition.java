package io.keyko.monitoring.agent.core.annotation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Mostly taken from org.springframework.boot.autoconfigure.condition.OnExpressionCondition
 */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class OnWebsocketCondition extends OnMultiExpressionCondition {

    private static final String WEBSOCKET_ENABLED_EXPRESSION =
            "'${ethereum.node.url}'.contains('wss://') || '${ethereum.node.url}'.contains('ws://')";

    private static final String WEBSOCKET_NOT_ENABLED_EXPRESSION =
            "!('${ethereum.node.url}'.contains('wss://') || '${ethereum.node.url}'.contains('ws://'))";

    public OnWebsocketCondition() {
        super(WEBSOCKET_ENABLED_EXPRESSION, WEBSOCKET_NOT_ENABLED_EXPRESSION, ConditionalOnWebsocket.class);
    }
}