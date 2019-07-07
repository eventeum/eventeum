package net.consensys.eventeum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import rx.Subscription;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class FilterSubscription {

    private ContractEventFilter filter;

    private Subscription subscription;

    private BigInteger startBlock;

    public FilterSubscription(ContractEventFilter filter, Subscription subscription) {
        this.filter = filter;
        this.subscription = subscription;
    }
}
