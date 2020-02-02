package io.keyko.monitoring.agent.core.model;

import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;
import lombok.Data;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class FilterSubscription {

    private ContractEventFilter filter;

    private Disposable subscription;

    private BigInteger startBlock;

    public FilterSubscription(ContractEventFilter filter, Disposable subscription) {
        this.filter = filter;
        this.subscription = subscription;
    }
}
