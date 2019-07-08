package net.consensys.eventeum.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.block.BlockDetails;
import org.springframework.data.annotation.Id;

import java.math.BigInteger;

@Data
@NoArgsConstructor
public class LatestBlock {

    public LatestBlock(BlockDetails blockDetails) {
        this.nodeName = blockDetails.getNodeName();
        this.number = blockDetails.getNumber();
        this.hash = blockDetails.getHash();
        this.timestamp = blockDetails.getTimestamp();
    }

    @Id
    private String nodeName;

    private BigInteger number;

    private String hash;

    private BigInteger timestamp;
}
