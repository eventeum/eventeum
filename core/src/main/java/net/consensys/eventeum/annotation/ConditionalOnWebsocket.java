package net.consensys.eventeum.annotation;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Configuration annotation for a conditional element that depends on kafka being required
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnWebsocketCondition.class)
public @interface ConditionalOnWebsocket {

    boolean value() default true;
}
