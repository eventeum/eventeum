package io.keyko.monitoring.agent.core.dto.message;

import io.keyko.monitoring.agent.core.model.TransactionMonitoringSpec;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TransactionMonitorAdded extends AbstractMessage<TransactionMonitoringSpec> {

    public static final String TYPE = "TRANSACTION_MONITOR_ADDED";

    public TransactionMonitorAdded(TransactionMonitoringSpec spec) {
        super(spec.getId(), TYPE, spec);
    }
}
