package net.consensys.eventeum.dto.event.filter.correlationId;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An abstract CorrelationIdStrategy that considers the correlation id of a specific contract event
 * to be the value of a parameter at a specified index.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@NoArgsConstructor
public abstract class ParameterCorrelationIdStrategy implements CorrelationIdStrategy {
    private int parameterIndex;

    private String type;

    protected ParameterCorrelationIdStrategy(String type, int parameterIndex) {
        this.type = type;
        this.parameterIndex = parameterIndex;
    }
}
