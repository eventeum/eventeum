package io.keyko.monitoring.agent.server.eventeumserver.integrationtest.utils;

import org.junit.runners.model.InitializationError;

public class RestartingSpringRunner extends RestartingSpringJUnit4ClassRunner {

    public RestartingSpringRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }
}
