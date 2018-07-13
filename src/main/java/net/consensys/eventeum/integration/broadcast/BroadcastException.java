package net.consensys.eventeum.integration.broadcast;

public class BroadcastException extends RuntimeException {

    public BroadcastException(String message) {
        super(message);
    }

    public BroadcastException(String message, Throwable t) {
        super(message, t);
    }
}
