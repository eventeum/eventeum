package io.keyko.monitoring.agent.core.chain.service.container;

import lombok.Data;
import io.keyko.monitoring.agent.core.chain.service.BlockchainService;
import org.web3j.protocol.Web3j;

@Data
public class NodeServices {

    private String nodeName;

    private Web3j web3j;

    private BlockchainService blockchainService;
}
