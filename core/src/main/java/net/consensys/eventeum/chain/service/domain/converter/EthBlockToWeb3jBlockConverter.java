package net.consensys.eventeum.chain.service.domain.converter;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jBlock;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.web3j.protocol.core.methods.response.EthBlock;

@NoArgsConstructor
public class EthBlockToWeb3jBlockConverter implements Converter<EthBlock.Block, Web3jBlock> {

  private static final String NONCE_ZERO = "0x0000000000000000";

  @Override
  public Web3jBlock convert(final MappingContext<EthBlock.Block, Web3jBlock> mappingContext) {
    final EthBlock.Block source = mappingContext.getSource();
    final Web3jBlock destination = mappingContext.getDestination();
    destination.setNumber(source.getNumber());
    destination.setHash(source.getHash());
    destination.setParentHash(source.getParentHash());
    destination.setNonce(
        source.getNonceRaw() == null || NONCE_ZERO.equals(source.getNonceRaw())
            ? null
            : source.getNonce());
    destination.setSha3Uncles(source.getSha3Uncles());
    destination.setLogsBloom(source.getLogsBloom());
    destination.setTransactionsRoot(source.getTransactionsRoot());
    destination.setStateRoot(source.getStateRoot());
    destination.setReceiptsRoot(source.getReceiptsRoot());
    destination.setAuthor(source.getAuthor());
    destination.setMiner(source.getMiner());
    destination.setMixHash(source.getMixHash());
    destination.setDifficulty(source.getDifficulty());
    destination.setTotalDifficulty(
        source.getTotalDifficultyRaw() != null ? source.getTotalDifficulty() : null);
    destination.setExtraData(source.getExtraData());
    destination.setSize(source.getSize());
    destination.setGasLimit(source.getGasLimit());
    destination.setGasUsed(source.getGasUsed());
    destination.setTimestamp(source.getTimestamp());
    destination.setUncles(source.getUncles());
    destination.setSealFields(source.getSealFields());

    return destination;
  }
}
