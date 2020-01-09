package net.consensys.eventeum.dto.message;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.ContractEventDetails;

@NoArgsConstructor
public class ContractEvent extends AbstractMessage<ContractEventDetails> {

    public static final String TYPE = "CONTRACT_EVENT";

    public ContractEvent(ContractEventDetails details) {
        super(details.getId(), TYPE, details);
    }
}
