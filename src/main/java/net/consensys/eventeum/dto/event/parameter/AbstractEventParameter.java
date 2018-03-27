package net.consensys.eventeum.dto.event.parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public abstract class AbstractEventParameter<T> implements EventParameter<T> {
    private String type;

    private T value;
}
