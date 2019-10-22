package net.consensys.eventeum.dto.event.parameter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * A parameter included within an event.
 *
 * @param <T> The java type that represents the value of the event.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonTypeIdResolver(ParameterTypeIdResolver.class)
public interface EventParameter<T extends Serializable> extends Serializable{
    String getType();

    T getValue();

    @JsonIgnore
    String getValueString();
}
