package net.consensys.eventeum.dto.event.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

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
    private List<ParameterDefinition> indexedParameterDefinitions;

    @ElementCollection
    private List<ParameterDefinition> nonIndexedParameterDefinitions;
}
