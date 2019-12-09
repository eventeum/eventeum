package net.consensys.eventeum.utils;

public class ExecutorNameFactory {

    public static final String build(String prefix, String nodeName) {
        return prefix.toUpperCase() + "-" + nodeName.toUpperCase();
    }
}
