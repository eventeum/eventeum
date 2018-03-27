package net.consensys.eventeum.dto.event.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.consensys.eventeum.dto.event.filter.correlationId.CorrelationIdStrategy;

/**
 * Represents the details of a contract event filter.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@EqualsAndHashCode
public class ContractEventFilter {

    private String id;

    private String contractAddress;

    private ContractEventSpecification eventSpecification;

    private CorrelationIdStrategy correlationIdStrategy;
}
