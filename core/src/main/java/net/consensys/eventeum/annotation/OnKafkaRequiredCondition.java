package net.consensys.eventeum.annotation;

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Mostly taken from org.springframework.boot.autoconfigure.condition.OnExpressionCondition
 */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class OnKafkaRequiredCondition extends SpringBootCondition {

    private static final String KAFKA_REQUIRED_EXPRESSION =
            "'${broadcaster.multiInstance}' == 'true' || '${broadcaster.type}' == 'KAFKA'";

    private static final String KAFKA_NOT_REQUIRED_EXPRESSION =
            "'${broadcaster.multiInstance}' == 'false' && '${broadcaster.type}' != 'KAFKA'";
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        boolean isKafkaRequired = (boolean) metadata
                .getAnnotationAttributes(ConditionalOnKafkaRequired.class.getName())
                .get("value");

        String expression =
                isKafkaRequired ? wrapIfNecessary(KAFKA_REQUIRED_EXPRESSION) : wrapIfNecessary(KAFKA_NOT_REQUIRED_EXPRESSION);
        String rawExpression = expression;
        expression = context.getEnvironment().resolvePlaceholders(expression);
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        BeanExpressionResolver resolver = (beanFactory != null)
                ? beanFactory.getBeanExpressionResolver() : null;
        BeanExpressionContext expressionContext = (beanFactory != null)
                ? new BeanExpressionContext(beanFactory, null) : null;
        if (resolver == null) {
            resolver = new StandardBeanExpressionResolver();
        }
        boolean result = (Boolean) resolver.evaluate(expression, expressionContext);
        return new ConditionOutcome(result, ConditionMessage
                .forCondition(ConditionalOnKafkaRequired.class, "(" + rawExpression + ")")
                .resultedIn(result));
    }

    /**
     * Allow user to provide bare expression with no '#{}' wrapper.
     * @param expression source expression
     * @return wrapped expression
     */
    private String wrapIfNecessary(String expression) {
        if (!expression.startsWith("#{")) {
            return "#{" + expression + "}";
        }
        return expression;
    }

}