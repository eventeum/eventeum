package net.consensys.eventeum.chain.service.domain.wrapper;

import lombok.Data;
import net.consensys.eventeum.chain.service.domain.Log;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.utils.ModelMapperFactory;
import org.modelmapper.Condition;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

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
