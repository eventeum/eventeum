package net.consensys.eventeum.dto.event.parameter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A parameter included within an event.
 *
 * @param <T> The java type that represents the value of the event.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@ParameterSubTypes
public interface EventParameter<T extends Serializable> extends Serializable{
    String getType();

    T getValue();

    @JsonIgnore
    String getValueString();
}
