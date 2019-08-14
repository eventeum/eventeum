package net.consensys.eventeum.dto.event.filter.correlationId;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.consensys.eventeum.dto.event.ContractEventDetails;

/**
 * A strategy for obtaining a correlation id for a given contract event.
 *
 * This is particularly useful when used with a Kafka broadcaster as you can configure the system
 * so that events with particular parameter values are always sent to the same partition.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Embeddable
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = IndexedParameterCorrelationIdStrategy.class,
            name = IndexedParameterCorrelationIdStrategy.TYPE),
        @JsonSubTypes.Type(value = NonIndexedParameterCorrelationIdStrategy.class,
                name = NonIndexedParameterCorrelationIdStrategy.TYPE)})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface CorrelationIdStrategy {
    String getType();

    @JsonIgnore
    String getCorrelationId(ContractEventDetails contractEvent);
}
