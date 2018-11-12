package net.consensys.eventeum.dto.block;

import lombok.Data;

import java.math.BigInteger;

/**
 * Represents the details of an Ethereum block.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
public class BlockDetails {
    private BigInteger number;

    private String hash;

    private BigInteger timestamp;
}
