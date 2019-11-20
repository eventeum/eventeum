package net.consensys.eventeum.chain.settings;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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
}
