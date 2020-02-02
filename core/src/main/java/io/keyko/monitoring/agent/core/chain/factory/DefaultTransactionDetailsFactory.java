package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.domain.Transaction;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;

@Component
public class DefaultTransactionDetailsFactory implements TransactionDetailsFactory {

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public TransactionDetails createTransactionDetails(
            Transaction transaction, TransactionStatus status, String nodeName) {

        final TransactionDetails transactionDetails = new TransactionDetails();
        modelMapper.map(transaction, transactionDetails);

        transactionDetails.setNodeName(nodeName);
        transactionDetails.setStatus(status);

        if (transaction.getCreates() != null) {
            transactionDetails.setContractAddress(Keys.toChecksumAddress(transaction.getCreates()));
        }

        return transactionDetails;
    }
}
