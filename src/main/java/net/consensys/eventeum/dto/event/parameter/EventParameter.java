package net.consensys.eventeum.dto.event.parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A parameter included within an event.
 *
 * @param <T> The java type that represents the value of the event.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class")
public interface EventParameter<T> {
    String getType();

    T getValue();

    @JsonIgnore
    String getValueString();
}
