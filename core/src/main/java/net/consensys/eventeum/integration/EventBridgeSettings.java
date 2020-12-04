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

package net.consensys.eventeum.integration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * An encapsulation of EventBridge related properties.
 *
 * @author Danilo Tuler <danilo.tuler@cartesi.io>
 */
@Configuration
@Data
@ConditionalOnProperty(name="broadcaster.type", havingValue="EVENT_BRIDGE")
public class EventBridgeSettings {

    @Value("${eventBridge.eventBusName}")
    private String eventBusName;

    @Value("${eventBridge.source}")
    private String source;

}
