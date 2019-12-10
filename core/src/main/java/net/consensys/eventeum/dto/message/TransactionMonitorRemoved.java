package net.consensys.eventeum.dto.message;

import java.io.Serializable;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.model.TransactionMonitoringSpec;

@NoArgsConstructor
public class TransactionMonitorRemoved extends AbstractMessage<TransactionMonitoringSpec> implements Serializable {

    public static final String TYPE = "TRANSACTION_MONITOR_REMOVED";

    public TransactionMonitorRemoved(TransactionMonitoringSpec spec) {
        super(spec.getId(), TYPE, spec);
    }
}
