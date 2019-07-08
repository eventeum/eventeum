package net.consensys.eventeum.dto.message;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.transaction.TransactionDetails;

@NoArgsConstructor
public class TransactionEvent extends AbstractMessage<TransactionDetails> {

    public static final String TYPE = "TRANSACTION";

    public TransactionEvent(TransactionDetails details) {
        super(details.getHash(), TYPE, details);
    }
}
