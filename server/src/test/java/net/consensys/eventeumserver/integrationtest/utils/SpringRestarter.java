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

package net.consensys.eventeumserver.integrationtest.utils;

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
