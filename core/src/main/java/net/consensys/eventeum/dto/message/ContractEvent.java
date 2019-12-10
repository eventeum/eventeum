package net.consensys.eventeum.dto.message;

import java.io.Serializable;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.event.ContractEventDetails;

@NoArgsConstructor
public class ContractEvent extends AbstractMessage<ContractEventDetails> implements Serializable{

    public static final String TYPE = "CONTRACT_EVENT";

    public ContractEvent(ContractEventDetails details) {
        super(details.getId(), TYPE, details);
    }
}
