package io.keyko.monitoring.agent.core.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class MicrometerValueMonitor implements EventeumValueMonitor {

    private static final String NAME_FORMAT = "eventeum.%s.%s";

    private MeterRegistry registry;

    public MicrometerValueMonitor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T extends Number> T monitor(String name, String nodeName, T number) {
        return registry.gauge(getGaugeName(name, nodeName), Tags.of("chain", nodeName), number);
    }

    private String getGaugeName(String name, String nodeName) {
        return String.format(NAME_FORMAT, nodeName, name);
    }
}
