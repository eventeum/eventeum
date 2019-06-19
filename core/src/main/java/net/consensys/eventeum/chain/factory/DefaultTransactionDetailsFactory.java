package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

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

        return transactionDetails;
    }
}
