package net.consensys.eventeum.dto.message;

import java.io.Serializable;

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
public interface EventeumMessage<T> extends RetryableMessage, Serializable{
    String getId();

    String getType();

    T getDetails();
}
