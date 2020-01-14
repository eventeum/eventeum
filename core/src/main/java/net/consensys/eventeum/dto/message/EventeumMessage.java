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

package net.consensys.eventeum.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.consensys.kafkadl.message.RetryableMessage;

/**
 * A message interface to be broadcast from the Eventeum application.
 *
 * @param <T> The details type for the message
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BlockEvent.class, name = BlockEvent.TYPE),
        @JsonSubTypes.Type(value = ContractEvent.class, name = ContractEvent.TYPE),
        @JsonSubTypes.Type(value = TransactionEvent.class, name = TransactionEvent.TYPE),
        @JsonSubTypes.Type(value = ContractEventFilterAdded.class, name = ContractEventFilterAdded.TYPE),
        @JsonSubTypes.Type(value = ContractEventFilterRemoved.class, name = ContractEventFilterRemoved.TYPE),
        @JsonSubTypes.Type(value = TransactionMonitorAdded.class, name = TransactionMonitorAdded.TYPE),
        @JsonSubTypes.Type(value = TransactionMonitorRemoved.class, name = TransactionMonitorRemoved.TYPE)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface EventeumMessage<T> extends RetryableMessage {
    String getId();

    String getType();

    T getDetails();
}