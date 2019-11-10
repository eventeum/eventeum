package net.consensys.eventeum.testutils;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.service.AsyncTaskService;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@NoArgsConstructor
public class DummyAsyncTaskService implements AsyncTaskService {

    @Override
    public void execute(String executorName, Runnable task) {
        task.run();
    }

    @Override
    public <T> Future<T> submit(String executorName, Callable<T> task) {
        try {
            return CompletableFuture.completedFuture(task.call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}