package net.consensys.eventeum.dto.event.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.filter.correlationId.CorrelationIdStrategy;

import java.math.BigInteger;

/**
 * Represents the details of a contract event filter.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractEventFilter {

    private String id;

    private String contractAddress;

    private String node = Constants.DEFAULT_NODE_NAME;

    private ContractEventSpecification eventSpecification;

    private CorrelationIdStrategy correlationIdStrategy;

    private BigInteger startBlock;
}
