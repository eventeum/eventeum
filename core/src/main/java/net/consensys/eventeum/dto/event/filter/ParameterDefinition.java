package net.consensys.eventeum.dto.event.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDefinition implements Comparable<ParameterDefinition>, Serializable {

    private Integer position;

    @Embedded
    private ParameterType type;

    private String name;

    @Override
    public int compareTo(ParameterDefinition o) {
        return this.position.compareTo(o.getPosition());
    }
}
