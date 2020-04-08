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

import java.lang.annotation.Annotation;

/**
 * Mostly taken from org.springframework.boot.autoconfigure.condition.OnExpressionCondition
 */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class OnMultiExpressionCondition extends SpringBootCondition {

    private String requiredExpression;
    private String notRequiredExpression;
    private Class<? extends Annotation> annotationClass;

    public OnMultiExpressionCondition(String requiredExpression,
                                      String notRequiredExpression, Class<? extends Annotation> annotationClass) {
        super();

        this.requiredExpression = requiredExpression;
        this.notRequiredExpression = notRequiredExpression;
        this.annotationClass = annotationClass;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        boolean isRequired = (boolean) metadata
                .getAnnotationAttributes(annotationClass.getName())
                .get("value");

        String expression =
                isRequired ? wrapIfNecessary(requiredExpression) : wrapIfNecessary(notRequiredExpression);
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
                .forCondition(annotationClass, "(" + rawExpression + ")")
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