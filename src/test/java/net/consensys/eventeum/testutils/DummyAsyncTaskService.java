package net.consensys.eventeum.testutils;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.service.AsyncTaskService;

@NoArgsConstructor
public class DummyAsyncTaskService implements AsyncTaskService {

    @Override
    public void execute(Runnable task) {
        task.run();
    }
}