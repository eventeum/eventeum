package io.keyko.monitoring.agent.core.annotation;

import io.keyko.monitoring.agent.core.config.DatabaseConfiguration;
import io.keyko.monitoring.agent.core.config.BaseConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({BaseConfiguration.class, DatabaseConfiguration.class})
public @interface EnableEventeum {
}
