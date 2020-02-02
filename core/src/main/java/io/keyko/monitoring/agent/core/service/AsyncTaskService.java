package io.keyko.monitoring.agent.core.service;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * An service that should execute a task in an Asynchronous manner.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface AsyncTaskService {

    void execute(String executorName, Runnable task);

    <T> Future<T> submit(String executorName, Callable<T> task);
}
