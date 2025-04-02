package net.consensys.eventeum.chain.service.domain.io;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigInteger;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractResultResponse {

  private String address;

  private BigInteger amount;

  private String bloom;

  private String callResult;

  private String contractId;

  private List<String> createdContractIds;

  private String errorMessage;

  private String from;

  private String functionParameters;

  private BigInteger gasLimit;

  private BigInteger gasUsed;

  private String hash;

  private String result;

  private String status;

  private String timestamp;

  private String to;

  private String accessList;

  private BigInteger blockGasUsed;

  private String blockHash;

  private String blockNumber;

  private String chainId;

  private String failedInitcode;

  private String gasPrice;

  private List<HederaLogResponse> logs;

  private String maxFeePerGas;

  private String maxPriorityFeePerGas;

  private BigInteger nonce;

  private String r;

  private String s;

  private BigInteger transactionIndex;

  private List<StateChangeResponse> stateChanges;

  private String type;

  private long v;
}
