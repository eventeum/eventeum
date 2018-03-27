package net.consensys.eventeum.service;

/**
 * An service that should execute a task in an Asynchronous manner.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface AsyncTaskService {

    void execute(Runnable task);
}
