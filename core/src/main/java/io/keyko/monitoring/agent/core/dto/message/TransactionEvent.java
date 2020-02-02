package io.keyko.monitoring.agent.core.dto.message;

import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TransactionEvent extends AbstractMessage<TransactionDetails> {

    public static final String TYPE = "TRANSACTION";

    public TransactionEvent(TransactionDetails details) {
        super(details.getHash(), TYPE, details);
    }
}
