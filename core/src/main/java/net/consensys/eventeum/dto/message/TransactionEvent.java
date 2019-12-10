package net.consensys.eventeum.dto.message;

import java.io.Serializable;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;

@NoArgsConstructor
public class TransactionEvent extends AbstractMessage<TransactionDetails> implements Serializable {

    public static final String TYPE = "TRANSACTION";

    public TransactionEvent(TransactionDetails details) {
        super(details.getHash(), TYPE, details);
    }
}
