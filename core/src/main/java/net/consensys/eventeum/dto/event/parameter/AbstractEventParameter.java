package net.consensys.eventeum.dto.event.parameter;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public abstract class AbstractEventParameter<T extends Serializable> implements EventParameter<T> {

    private String type;

    private T value;

    private String name;
}
