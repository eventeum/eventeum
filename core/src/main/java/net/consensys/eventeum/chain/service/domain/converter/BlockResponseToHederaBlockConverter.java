package net.consensys.eventeum.chain.service.domain.converter;

import java.util.Collections;
import net.consensys.eventeum.chain.service.domain.io.BlockResponse;
import net.consensys.eventeum.chain.service.domain.wrapper.HederaBlock;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class BlockResponseToHederaBlockConverter implements Converter<BlockResponse, HederaBlock> {

  @Override
  public HederaBlock convert(MappingContext<BlockResponse, HederaBlock> mappingContext) {
    BlockResponse blockResponse = mappingContext.getSource();
    HederaBlock hederaBlock = new HederaBlock();
    hederaBlock.setCount(blockResponse.getCount());
    hederaBlock.setGasUsed(blockResponse.getGasUsed());
    hederaBlock.setHapiVersion(blockResponse.getHapiVersion());
    hederaBlock.setHash(blockResponse.getHash());
    hederaBlock.setLogsBloom(blockResponse.getLogsBloom());
    hederaBlock.setName(blockResponse.getName());
    hederaBlock.setNumber(blockResponse.getNumber());
    hederaBlock.setPreviousHash(blockResponse.getPreviousHash());
    hederaBlock.setSize(blockResponse.getSize());
    hederaBlock.setFromTimestamp(blockResponse.getTimestamp().getFrom());
    hederaBlock.setToTimestamp(blockResponse.getTimestamp().getTo());
    hederaBlock.setTransactions(Collections.emptyList());

    return hederaBlock;
  }
}
