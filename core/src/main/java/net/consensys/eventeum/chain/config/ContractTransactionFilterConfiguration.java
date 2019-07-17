package net.consensys.eventeum.chain.config;

import lombok.Data;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties
@Data
public class ContractTransactionFilterConfiguration {
    private List<TransactionMonitoringSpec> contractTransactionFilters;

    public List<TransactionMonitoringSpec> getConfiguredEventFilters() {
        List<TransactionMonitoringSpec> filtersToReturn = new ArrayList<>();

        if (contractTransactionFilters != null) {

            contractTransactionFilters.forEach((configFilter) -> {
                final TransactionMonitoringSpec contractTransactionFilter = new TransactionMonitoringSpec(
                        configFilter.getType(),
                        configFilter.getTransactionIdentifierValue(),
                        configFilter.getNodeName(),
                        configFilter.getStatuses()
                );

                filtersToReturn.add(contractTransactionFilter);
            });
        }

        return filtersToReturn;
    }
}
