package io.keyko.monitoring.agent.server.eventeumserver.integrationtest.utils;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class RestartingSpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {

    public RestartingSpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Object createTest() throws Exception {
        final Object testInstance = super.createTest();

        SpringRestarter.getInstance().init(getTestContextManager());

        return testInstance;
    }
}
