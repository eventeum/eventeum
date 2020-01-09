package net.consensys.eventeum.monitoring;

public interface EventeumValueMonitor {

    <T extends Number> T monitor(String name, String node, T number);
}
