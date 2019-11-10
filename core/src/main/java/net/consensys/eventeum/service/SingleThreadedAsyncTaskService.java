package net.consensys.eventeum.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An async task service that utilises a single thread executor
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class SingleThreadedAsyncTaskService implements AsyncTaskService {

    private Map<String, ExecutorService> executorServices = new HashMap<>();

    @Override
    public void execute(String executorName, Runnable task) {
        getOrCreateExecutor(executorName).execute(task);
    }

    @Override
    public <T> Future<T> submit(String executorName, Callable<T> task) {
        return getOrCreateExecutor(executorName).submit(task);
    }

    private ExecutorService getOrCreateExecutor(String executorName) {
        if (!executorServices.containsKey(executorName)) {
            executorServices.put(executorName, buildExecutor());
        }

        return executorServices.get(executorName);
    }

    protected ExecutorService buildExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
