package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import org.modelmapper.ModelMapper;

public class DefaultTransactionDetailsFactory implements TransactionDetailsFactory {

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public TransactionDetails createTransactionDetails(Transaction transaction, String nodeName) {

        final TransactionDetails transactionDetails = new TransactionDetails();
        modelMapper.map(transaction, transactionDetails);

        transactionDetails.setNodeName(nodeName);

        return transactionDetails;
    }
}
