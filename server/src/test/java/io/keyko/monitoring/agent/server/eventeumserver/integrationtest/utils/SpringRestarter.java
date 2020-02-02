package io.keyko.monitoring.agent.server.eventeumserver.integrationtest.utils;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

public class SpringRestarter {

    private static SpringRestarter INSTANCE = null;

    private TestContextManager testContextManager;

    public static SpringRestarter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpringRestarter();
        }

        return INSTANCE;
    }

    public void init(TestContextManager testContextManager) {
        this.testContextManager = testContextManager;
    }

    public void restart(Runnable stoppedLogic) {
        testContextManager.getTestContext().markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);

        if (stoppedLogic != null) {
            stoppedLogic.run();
        }

        testContextManager.getTestContext().getApplicationContext();
        reinjectDependencies();
    }

    private void reinjectDependencies()  {
        testContextManager
                .getTestExecutionListeners()
                .stream()
                .filter(listener -> listener instanceof DependencyInjectionTestExecutionListener)
                .findFirst()
                .ifPresent(listener -> {
                    try {
                        listener.prepareTestInstance(testContextManager.getTestContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
