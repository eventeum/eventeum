package io.keyko.monitoring.agent.core.dto.event.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents contract event specification, to be used when registering a new filter.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Embeddable
@Data
@EqualsAndHashCode
public class ContractEventSpecification implements Serializable {

    private String eventName;

    @ElementCollection
    // See answer in https://stackoverflow.com/questions/51835604/jpa-elementcollection-within-embeddable-not-persisted
    private List<ParameterDefinition> indexedParameterDefinitions = new ArrayList<>();

    @ElementCollection
    private List<ParameterDefinition> nonIndexedParameterDefinitions = new ArrayList<>();
}
