package io.keyko.monitoring.agent.core.chain.service.domain.wrapper;

import io.keyko.monitoring.agent.core.utils.ModelMapperFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.keyko.monitoring.agent.core.chain.service.domain.Log;
import org.modelmapper.ModelMapper;

import java.math.BigInteger;
import java.util.List;

/**
 * A Log that is constructed from a Web3j Log object.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@NoArgsConstructor
public class Web3jLog implements Log {

    private boolean removed;
    private BigInteger logIndex;
    private BigInteger transactionIndex;
    private String transactionHash;
    private String blockHash;
    private BigInteger blockNumber;
    private String address;
    private String data;
    private String type;
    private List<String> topics;

    public Web3jLog(org.web3j.protocol.core.methods.response.Log web3jLog) {

        final ModelMapper modelMapper = ModelMapperFactory.getInstance().createModelMapper();
        modelMapper.map(web3jLog, this);
    }
}
