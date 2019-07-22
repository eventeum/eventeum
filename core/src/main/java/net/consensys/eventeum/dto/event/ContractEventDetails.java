package net.consensys.eventeum.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.consensys.eventeum.dto.event.parameter.EventParameter;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Represents the details of an emitted Ethereum smart contract event.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Entity
@Data
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractEventDetails {

    private String name;

    @Id
    private String filterId;

    private String nodeName;

    @ElementCollection
    private List<EventParameter> indexedParameters;

    @ElementCollection
    private List<EventParameter> nonIndexedParameters;

    private String transactionHash;

    private BigInteger logIndex;

    private BigInteger blockNumber;

    private String blockHash;

    private String address;

    private ContractEventStatus status = ContractEventStatus.UNCONFIRMED;

    private String eventSpecificationSignature;

    private String networkName;

    public String getId() {
        return transactionHash + "-" + blockHash + "-" + logIndex;
    }
}
