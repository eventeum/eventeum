package net.consensys.eventeum.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An async task service that utilises a cached thread pool executor.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class PooledAsyncTaskService implements AsyncTaskService {

    private ExecutorService executerService;

    public PooledAsyncTaskService() {
        executerService = Executors.newCachedThreadPool();
    }

    @Override
    public void execute(Runnable task) {
        executerService.execute(task);
    }
}
