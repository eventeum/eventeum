/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.monitoring;

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
