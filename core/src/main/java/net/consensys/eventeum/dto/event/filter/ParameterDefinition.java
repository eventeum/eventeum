package net.consensys.eventeum.dto.event.filter;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OrderColumn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDefinition implements Comparable<ParameterDefinition>, Serializable {

    private Integer position;

    @Enumerated(EnumType.ORDINAL)
    private ParameterType type;

    @Override
    public int compareTo(ParameterDefinition o) {
        return this.position.compareTo(o.getPosition());
    }
}
