package io.keyko.monitoring.agent.core.dto.event.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.keyko.monitoring.agent.core.dto.event.filter.correlationId.CorrelationIdStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.keyko.monitoring.agent.core.constant.Constants;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;

/**
 * Represents the details of a contract event filter.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Document
@Entity
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractEventFilter {

    @Id
    private String id;

    private String contractAddress;

    private String node = Constants.DEFAULT_NODE_NAME;

    @Embedded
    private ContractEventSpecification eventSpecification;

    @Embedded
    private CorrelationIdStrategy correlationIdStrategy;

    private BigInteger startBlock;
}
