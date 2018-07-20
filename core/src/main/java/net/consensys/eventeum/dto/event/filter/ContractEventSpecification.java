package net.consensys.eventeum.dto.event.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents contract event specification, to be used when registering a new filter.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@EqualsAndHashCode
public class ContractEventSpecification {

    private String eventName;

    private List<ParameterDefinition> indexedParameterDefinitions;

    private List<ParameterDefinition> nonIndexedParameterDefinitions;
}
