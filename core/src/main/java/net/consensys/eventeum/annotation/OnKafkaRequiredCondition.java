package net.consensys.eventeum.annotation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Mostly taken from org.springframework.boot.autoconfigure.condition.OnExpressionCondition
 */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class OnKafkaRequiredCondition extends OnMultiExpressionCondition {

    private static final String KAFKA_REQUIRED_EXPRESSION =
            "'${broadcaster.multiInstance}:${broadcaster.multiInstanceType}' == 'true:Kafka' || '${broadcaster.type}' == 'KAFKA'";

    private static final String KAFKA_NOT_REQUIRED_EXPRESSION =
            "'${broadcaster.multiInstance}' == 'false' && '${broadcaster.type}' != 'KAFKA'";

    public OnKafkaRequiredCondition() {
        super(KAFKA_REQUIRED_EXPRESSION, KAFKA_NOT_REQUIRED_EXPRESSION, ConditionalOnKafkaRequired.class);
    }
}