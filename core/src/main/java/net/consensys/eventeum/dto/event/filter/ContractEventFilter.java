package net.consensys.eventeum.dto.event.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractEventFilter {

    public static final String DEFAULT_NODE_NAME = "default";

    private String id;

    private String contractAddress;

    private String node = DEFAULT_NODE_NAME;

    private ContractEventSpecification eventSpecification;

    private CorrelationIdStrategy correlationIdStrategy;
}
