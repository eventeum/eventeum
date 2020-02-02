package io.keyko.monitoring.agent.core.chain.service.domain.wrapper;

import io.keyko.monitoring.agent.core.utils.ModelMapperFactory;
import lombok.Data;
import io.keyko.monitoring.agent.core.chain.service.domain.Log;
import io.keyko.monitoring.agent.core.chain.service.domain.TransactionReceipt;
import org.modelmapper.ModelMapper;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A TransactionReceipt that is constructed from a Web3j transaction receipt.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
public class Web3jTransactionReceipt implements TransactionReceipt {

    private String transactionHash;
    private BigInteger transactionIndex;
    private String blockHash;
    private BigInteger blockNumber;
    private BigInteger cumulativeGasUsed;
    private BigInteger gasUsed;
    private String contractAddress;
    private String root;
    private String from;
    private String to;
    private List<Log> logs;
    private String logsBloom;
    private String status;

    public Web3jTransactionReceipt(
            org.web3j.protocol.core.methods.response.TransactionReceipt web3TransactionReceipt) {

        logs = convertLogs(web3TransactionReceipt.getLogs());

        try {
            final ModelMapper modelMapper = ModelMapperFactory.getInstance().createModelMapper();
            //Skip logs
            modelMapper.getConfiguration().setPropertyCondition(ctx ->
                    !ctx.getMapping().getLastDestinationProperty().getName().equals("logs"));
            modelMapper.map(web3TransactionReceipt, this);
        } catch (RuntimeException re) {
            re.printStackTrace();
            throw re;
        }
    }

    private List<Log> convertLogs(List<org.web3j.protocol.core.methods.response.Log> logs) {
        return logs.stream()
                .map(log -> new Web3jLog(log))
                .collect(Collectors.toList());
    }
}
