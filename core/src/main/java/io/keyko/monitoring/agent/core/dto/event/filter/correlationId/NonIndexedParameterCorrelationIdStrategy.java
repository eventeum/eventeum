package io.keyko.monitoring.agent.core.dto.event.filter.correlationId;

import lombok.NoArgsConstructor;
import io.keyko.monitoring.agent.core.dto.event.ContractEventDetails;

/**
 * A CorrelationIdStrategy that considers the correlation id of a specific contract event
 * to be the value of an non-indexed parameter at a specified index.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@NoArgsConstructor
public class NonIndexedParameterCorrelationIdStrategy extends ParameterCorrelationIdStrategy {

    public static final String TYPE = "NON_INDEXED_PARAMETER";

    public NonIndexedParameterCorrelationIdStrategy(int parameterIndex) {
        super(TYPE, parameterIndex);
    }

    @Override
    public String getCorrelationId(ContractEventDetails contractEvent) {
        return contractEvent
                .getNonIndexedParameters()
                .get(getParameterIndex())
                .getValueString();
    }
}
