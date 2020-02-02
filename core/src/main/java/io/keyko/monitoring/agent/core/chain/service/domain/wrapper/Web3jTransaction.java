package io.keyko.monitoring.agent.core.chain.service.domain.wrapper;

import io.keyko.monitoring.agent.core.utils.ModelMapperFactory;
import lombok.Data;
import io.keyko.monitoring.agent.core.chain.service.domain.Transaction;
import org.modelmapper.ModelMapper;

@Data
public class Web3jTransaction implements Transaction {

    private String hash;
    private String nonce;
    private String blockHash;
    private String blockNumber;
    private String transactionIndex;
    private String from;
    private String to;
    private String value;
    private String gasPrice;
    private String gas;
    private String input;
    private String creates;
    private String publicKey;
    private String raw;
    private String r;
    private String s;
    private long v;

    public Web3jTransaction(org.web3j.protocol.core.methods.response.Transaction web3jTransaction) {

        final ModelMapper modelMapper = ModelMapperFactory.getInstance().createModelMapper();
        modelMapper.map(web3jTransaction, this);
    }
}
