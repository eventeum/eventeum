package net.consensys.eventeum.dto.message;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.model.TransactionMonitoringSpec;

@NoArgsConstructor
public class TransactionMonitorRemoved extends AbstractMessage<TransactionMonitoringSpec> {

    public static final String TYPE = "TRANSACTION_MONITOR_REMOVED";

    public TransactionMonitorRemoved(TransactionMonitoringSpec spec) {
        super(spec.getId(), TYPE, spec);
    }
}
