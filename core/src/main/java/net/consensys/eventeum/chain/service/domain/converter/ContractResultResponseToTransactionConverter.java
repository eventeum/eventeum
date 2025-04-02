package net.consensys.eventeum.chain.service.domain.converter;

import net.consensys.eventeum.chain.service.domain.io.ContractResultResponse;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jTransaction;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class ContractResultResponseToTransactionConverter
    implements Converter<ContractResultResponse, Web3jTransaction> {

  @Override
  public Web3jTransaction convert(
      MappingContext<ContractResultResponse, Web3jTransaction> mappingContext) {
    ContractResultResponse res = mappingContext.getSource();
    Web3jTransaction tx = new Web3jTransaction();
    tx.setHash(res.getHash());
    //        tx.setNonce(res.getNonce().toString());
    tx.setBlockHash(res.getBlockHash());
    tx.setBlockNumber(res.getBlockNumber());
    tx.setTransactionIndex(
        res.getTransactionIndex() != null ? res.getTransactionIndex().toString() : null);
    tx.setFrom(res.getFrom());
    tx.setTo(res.getTo());
    tx.setValue(res.getAmount() != null ? res.getAmount().toString() : null);
    //        tx.setGasPrice(res.getGasPrice());
    //        tx.setGas(res.getGasUsed().toString());
    tx.setInput(res.getFunctionParameters());
    tx.setCreates("");
    tx.setPublicKey("");
    tx.setRaw("");
    //        tx.setR(res.getR());
    //        tx.setV(res.getV());
    return tx;
  }
}
