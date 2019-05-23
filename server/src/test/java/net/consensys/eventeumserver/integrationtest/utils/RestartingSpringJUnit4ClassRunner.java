package net.consensys.eventeumserver.integrationtest.utils;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class RestartingSpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {

    public RestartingSpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected TestContextManager createTestContextManager(Class<?> clazz) {

        final TestContextManager testContextManager = super.createTestContextManager(clazz);

        SpringRestarter.getInstance().init(testContextManager);
        return testContextManager;
    }
}
