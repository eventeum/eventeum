package net.consensys.eventeum.chain.util;

import lombok.Data;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BloomFilterUtil {

    private static final int LEAST_SIGNIFICANT_THREE_BITS = 0x7;

    public static BloomFilterBits getBloomBits(ContractEventFilter filter) {
        final byte[] contractAddressHash = Hash.sha3(Numeric.hexStringToByteArray(filter.getContractAddress()));

        final List<Integer> contractAddressBits = getBloomBits(contractAddressHash);

        final byte[] eventSpecHash = Hash.sha3(Numeric.hexStringToByteArray(
                Web3jUtil.getSignature(filter.getEventSpecification())));

        final List<Integer> eventSpecBits = getBloomBits(eventSpecHash);

        return new BloomFilterBits(contractAddressBits, eventSpecBits);

    }

    public static List<Integer> getBloomBits(byte[] bytes) {
        final List<Integer> bits = new ArrayList<>();
        for (int counter = 0; counter < 6; counter += 2) {
            final int bloomBit =
                    ((bytes[counter] & LEAST_SIGNIFICANT_THREE_BITS) << 8) + (bytes[counter + 1] & 0xFF);

            bits.add(bloomBit);
        }

        return bits;
    }

    public static boolean bloomFilterMatch(String bloomFilterHex, BloomFilterBits bitsToMatch) {
        final byte[] bloomFilterBytes = Numeric.hexStringToByteArray(bloomFilterHex);

        return !bitsToMatch.getBitIndexes()
                .stream()
                .filter(bit -> {
                    final int byteIndex = 256 - 1 - bit / 8;
                    final int bitIndex = bit % 8;

                    final byte theByte = bloomFilterBytes[byteIndex];
                    return ((theByte >> bitIndex) & 1) == 0;
                })
                .findFirst()
                .isPresent();
    }

    @Data
    public static class BloomFilterBits {
        private List<Integer> bitIndexes;

        private BloomFilterBits(List<Integer> ... bits) {
            bitIndexes = Stream.of(bits)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }
}
