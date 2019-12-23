package net.consensys.eventeum.monitoring;

import io.micrometer.core.instrument.Tag;

public interface EventeumValueMonitor {

    <T extends Number> T monitor(String name, String node, T number);
}
