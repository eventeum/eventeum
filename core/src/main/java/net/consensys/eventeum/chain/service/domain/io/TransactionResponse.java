package net.consensys.eventeum.chain.service.domain.io;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigInteger;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionResponse {

  private String bytes;

  private BigInteger chargedTxFee;

  private String consensusTimestamp;

  private String entityId;

  private String maxFee;

  private String memoBase64;

  private String name;

  private String node;

  private BigInteger nonce;

  private String parentConsensusTimestamp;

  private String result;

  private Boolean scheduled;

  private List<StakingRewardTransferResponse> stakingRewardTransfers;

  private String transactionHash;

  private String transactionId;

  private List<TokenTransfersResponse> tokenTransfers;

  private List<TransfersResponse> transfers;

  private BigInteger validDurationSeconds;

  private String validStartTimestamp;
}
