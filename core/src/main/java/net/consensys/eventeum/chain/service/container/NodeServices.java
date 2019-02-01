package net.consensys.eventeum.chain.service.container;

import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainService;
import org.web3j.protocol.Web3j;

@Data
public class NodeServices {

    private String nodeName;

    private Web3j web3j;

    private BlockchainService blockchainService;
}
