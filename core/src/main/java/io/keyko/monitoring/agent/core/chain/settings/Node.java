package io.keyko.monitoring.agent.core.chain.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {

    private String name;

    private String url;
    private Long pollingInterval;
    private String username;
    private String password;
    private String blockStrategy;
    private Boolean addTransactionRevertReason;
    private Integer maxIdleConnections;
    private Long keepAliveDuration;
    private Long connectionTimeout;
    private Long readTimeout;
    private Integer syncingThreshold;
    private Long healthcheckInterval;
    private BigInteger blocksToWaitForConfirmation;
    private BigInteger blocksToWaitForMissingTx;
    private BigInteger blocksToWaitBeforeInvalidating;
}
