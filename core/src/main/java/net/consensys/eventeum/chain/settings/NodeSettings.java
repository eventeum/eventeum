/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.chain.settings;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Data
@Component
public class NodeSettings {

  private static final Logger logger = LoggerFactory.getLogger(NodeSettings.class);

  private static final Long DEFAULT_POLLING_INTERVAL = 10000L;

  private static final Long DEFAULT_HEALTHCHECK_POLLING_INTERVAL = 10000L;

  private static final Long DEFAULT_KEEP_ALIVE_DURATION = 10000L;

  private static final Integer DEFAULT_MAX_IDLE_CONNECTIONS = 5;

  private static final Long DEFAULT_CONNECTION_TIMEOUT = 5000L;

  private static final Integer DEFAULT_SYNCING_THRESHOLD = 60;

  private static final Long DEFAULT_READ_TIMEOUT = 60000L;

  private static final BigInteger DEFAULT_CALL_RETRIES = BigInteger.TEN;

  private static final BigInteger DEFAULT_LIMIT_PER_REQUEST = BigInteger.valueOf(100);

  private static final String DEFAULT_BLOCKS_TO_WAIT_FOR_MISSING_TX = "200";

  private static final String DEFAULT_BLOCKS_TO_WAIT_BEFORE_INVALIDATING = "2";

  private static final String DEFAULT_BLOCKS_TO_WAIT_FOR_CONFIRMATION = "12";

  private static final String DEFAULT_NUM_BLOCKS_TO_REPLAY = "12";

  private static final String DEFAULT_MAX_BLOCKS_TO_SYNC = "20000";

  private static final String DEFAULT_LIMIT_MIRROR_NODE_RESULTS = "200";

  private static final String ATTRIBUTE_PREFIX = "ethereum";

  private static final String NODE_ATTRIBUTE_PREFIX = ".nodes[%s]";

  private static final String NODE_URL_ATTRIBUTE = "url";

  private static final String NODE_HEADERS_ATTRIBUTE = "headers";

  private static final String NODE_CALL_RETRIES_ATTRIBUTE = "callRetries";

  private static final String NODE_LIMIT_PER_REQUEST_ATTRIBUTE = "limitPerRequest";

  private static final String NODE_TYPE_ATTRIBUTE = "type";

  private static final String NODE_NAME_ATTRIBUTE = "name";

  private static final String ETHEREUM_CHAIN_NAME_ATTRIBUTE = "ethereum";

  private static final String HASHGRAPH_CHAIN_NAME_ATTRIBUTE = "hashgraph";

  private static final String NODE_USERNAME_ATTRIBUTE = "username";

  private static final String NODE_PASSWORD_ATTRIBUTE = "password";

  private static final String NODE_POLLING_INTERVAL_ATTRIBUTE = "pollingInterval";

  private static final String BLOCK_STRATEGY_ATTRIBUTE = "blockStrategy";

  private static final String TRANSACTION_REVERT_REASON_ATTRIBUTTE = "addTransactionRevertReason";

  private static final String MAX_IDLE_CONNECTIONS_ATTRIBUTTE = "maxIdleConnections";

  private static final String KEEP_ALIVE_DURATION_ATTRIBUTTE = "keepAliveDuration";

  private static final String READ_TIMEOUT_ATTRIBUTTE = "readTimeout";

  private static final String CONNECTION_TIMEOUT_ATTRIBUTE = "connectionTimeout";

  private static final String SYNCING_THRESHOLD_ATTRIBUTE = "syncingThreshold";

  private static final String NODE_HEALTHCHECK_INTERVAL_ATTRIBUTE = "healthcheckInterval";

  private static final String BLOCKS_TO_WAIT_FOR_CONFIRMATION_ATTRIBUTE = "numBlocksToWait";

  private static final String GLOBAL_BLOCKS_TO_WAIT_FOR_CONFIRMATION_ATTRIBUTE =
      "broadcaster.event.confirmation.numBlocksToWait";

  private static final String BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTRIBUTE =
      "numBlocksToWaitBeforeInvalidating";

  private static final String GLOBAL_BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTIBUTE =
      "broadcaster.event.confirmation.numBlocksToWaitBeforeInvalidating";

  private static final String BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE =
      "numBlocksToWaitForMissingTx";

  private static final String GLOBAL_BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE =
      "broadcaster.event.confirmation.numBlocksToWaitForMissingTx";

  private static final String INITIAL_START_BLOCK_ATTRIBUTE = "initialStartBlock";

  private static final String GLOBAL_INITIAL_START_BLOCK_ATTRIBUTE =
      "." + INITIAL_START_BLOCK_ATTRIBUTE;

  private static final String NUM_BLOCKS_TO_REPLAY_ATTRIBUTE = "numBlocksToReplay";

  private static final String GLOBAL_NUM_BLOCKS_TO_REPLAY_ATTRIBUTE =
      "." + NUM_BLOCKS_TO_REPLAY_ATTRIBUTE;

  private static final String MAX_BLOCKS_TO_SYNC_ATTRIBUTE = "maxBlocksToSync";

  private static final String EXTENSIONS_ATTRIBUTE = "extensions";

  private static final String LIMIT_MIRROR_NODE_RESULTS = "limitMirrorNodeResults";

  private static final String GLOBAL_MAX_BLOCKS_TO_SYNC_ATTRIBUTE =
      "." + MAX_BLOCKS_TO_SYNC_ATTRIBUTE;

  @Deprecated
  private static final String NODE_MAX_UNSYNCED_BLOCKS_FOR_FILTER_ATTRIBUTE =
      "maxUnsyncedBlocksForFilter";

  @Deprecated
  private static final String DEFAULT_MAX_UNSYNCED_BLOCKS_FOR_FILTER_ATTRIBUTE =
      "ethereum.maxUnsyncedBlocksForFilter";

  @Deprecated private static final String NODE_SYNC_START_BLOCK_ATTRIBUTE = "syncStartBlock";

  public static final String DEFAULT_SYNC_START_BLOCK = "-1";

  private static final String[] SUPPORTED_CHAINS =
      new String[] {ETHEREUM_CHAIN_NAME_ATTRIBUTE, HASHGRAPH_CHAIN_NAME_ATTRIBUTE};

  private HashMap<String, Node> nodes;

  private String blockStrategy;

  public NodeSettings(Environment environment) {
    populateNodeSettings(environment);

    blockStrategy = environment.getProperty(ATTRIBUTE_PREFIX + "." + BLOCK_STRATEGY_ATTRIBUTE);
  }

  public Node getNode(String nodeName) {
    return nodes.get(nodeName);
  }

  private void populateNodeSettings(Environment environment) {
    nodes = new HashMap<>();

    for (String supportedChain : SUPPORTED_CHAINS) {
      int index = 0;

      while (nodeExistsAtIndex(environment, supportedChain, index)) {
        String nodeName = getNodeNameProperty(environment, supportedChain, index);
        Node node =
            new Node(
                nodeName,
                getNodeTypeProperty(environment, supportedChain, index),
                ChainType.valueOf(supportedChain.toUpperCase()),
                getNodeUrlProperty(environment, supportedChain, index),
                getHeaders(environment, supportedChain, index),
                getNodeLimitPerRequestProperty(environment, supportedChain, index),
                getNodeCallRetriesProperty(environment, supportedChain, index),
                getNodePollingIntervalProperty(environment, supportedChain, index),
                getNodeUsernameProperty(environment, supportedChain, index),
                getNodePasswordProperty(environment, supportedChain, index),
                getNodeBlockStrategyProperty(environment, supportedChain, index),
                getNodeTransactionRevertReasonProperty(environment, supportedChain, index),
                getMaxIdleConnectionsProperty(environment, supportedChain, index),
                getKeepAliveDurationProperty(environment, supportedChain, index),
                getConnectionTimeoutProperty(environment, supportedChain, index),
                getReadTimeoutProperty(environment, supportedChain, index),
                getSyncingThresholdProperty(environment, supportedChain, index),
                getNodeHealthcheckIntervalProperty(environment, supportedChain, index),
                getBlocksToWaitForConfirmationProperty(environment, supportedChain, index),
                getBlocksToWaitBeforeInvalidatingProperty(environment, supportedChain, index),
                getBlocksToWaitForMissingTxProperty(environment, supportedChain, index),
                getInitialStartBlockProperty(environment, supportedChain, index),
                getNumBlocksToReplayProperty(environment, supportedChain, index),
                getMaxBlocksToSyncProperty(environment, supportedChain, index),
                getExtensions(environment, supportedChain, index),
                getLimitMirrorNodeResults(environment, supportedChain, index));

        nodes.put(nodeName, node);

        index++;
      }
    }

    if (nodes.isEmpty()) {
      throw new BlockchainException("No nodes configured!");
    }
  }

  private Map<String, Object> getExtensions(Environment environment, String chainName, int index) {
    return Binder.get(environment)
        .bind(buildNodeAttribute(EXTENSIONS_ATTRIBUTE, chainName, index), Map.class)
        .orElse(null);
  }

  private BigInteger getLimitMirrorNodeResults(
      Environment environment, String chainName, int index) {
    String limitMirrorNodeResults =
        getProperty(environment, buildNodeAttribute(LIMIT_MIRROR_NODE_RESULTS, chainName, index));

    if (limitMirrorNodeResults == null) {
      // Get the generic configuration
      limitMirrorNodeResults = environment.getProperty("hashgraph.nodes.limitMirrorNodeResults");
    }

    if (limitMirrorNodeResults == null) {
      // Get the default configuration
      limitMirrorNodeResults = DEFAULT_LIMIT_MIRROR_NODE_RESULTS;
    }

    return BigInteger.valueOf(Long.valueOf(limitMirrorNodeResults));
  }

  private boolean nodeExistsAtIndex(Environment environment, String chainName, int index) {
    return environment.containsProperty(buildNodeAttribute(NODE_NAME_ATTRIBUTE, chainName, index));
  }

  private String getNodeNameProperty(Environment environment, String chainName, int index) {
    return getProperty(environment, buildNodeAttribute(NODE_NAME_ATTRIBUTE, chainName, index));
  }

  private NodeType getNodeTypeProperty(Environment environment, String chainName, int index) {
    String nodeType =
        getProperty(environment, buildNodeAttribute(NODE_TYPE_ATTRIBUTE, chainName, index));
    if (nodeType == null) {
      nodeType = NodeType.NORMAL.getNodeName();
    }
    if (chainName.equals(ETHEREUM_CHAIN_NAME_ATTRIBUTE)) {
      if (!nodeType.equals(NodeType.NORMAL.getNodeName())) {
        throw new BlockchainException("Ethereum only supports normal nodes!");
      }
    }
    return NodeType.valueOf(nodeType);
  }

  private String getNodeUrlProperty(Environment environment, String chainName, int index) {
    return getProperty(environment, buildNodeAttribute(NODE_URL_ATTRIBUTE, chainName, index));
  }

  private BigInteger getNodeCallRetriesProperty(
      Environment environment, String chainName, int index) {
    final String nodeCallRetries =
        getProperty(environment, buildNodeAttribute(NODE_CALL_RETRIES_ATTRIBUTE, chainName, index));

    if (nodeCallRetries == null) {
      return DEFAULT_CALL_RETRIES;
    }

    return BigInteger.valueOf(Long.parseLong(nodeCallRetries));
  }

  private BigInteger getNodeLimitPerRequestProperty(
      Environment environment, String chainName, int index) {
    final String nodeLimitPerRequest =
        getProperty(
            environment, buildNodeAttribute(NODE_LIMIT_PER_REQUEST_ATTRIBUTE, chainName, index));

    if (nodeLimitPerRequest == null) {
      return DEFAULT_LIMIT_PER_REQUEST;
    }

    return BigInteger.valueOf(Long.parseLong(nodeLimitPerRequest));
  }

  private Map<String, String> getHeaders(Environment environment, String chainName, int index) {
    return Binder.get(environment)
        .bind(buildNodeAttribute(NODE_HEADERS_ATTRIBUTE, chainName, index), Map.class)
        .orElse(null);
  }

  private Long getNodePollingIntervalProperty(
      Environment environment, String chainName, int index) {
    final String pollingInterval =
        getProperty(
            environment, buildNodeAttribute(NODE_POLLING_INTERVAL_ATTRIBUTE, chainName, index));

    if (pollingInterval == null) {
      return DEFAULT_POLLING_INTERVAL;
    }

    return Long.valueOf(pollingInterval);
  }

  private Integer getMaxIdleConnectionsProperty(
      Environment environment, String chainName, int index) {
    final String maxIdleConnections =
        getProperty(
            environment, buildNodeAttribute(MAX_IDLE_CONNECTIONS_ATTRIBUTTE, chainName, index));

    if (maxIdleConnections == null) {
      return DEFAULT_MAX_IDLE_CONNECTIONS;
    }

    return Integer.valueOf(maxIdleConnections);
  }

  private Long getKeepAliveDurationProperty(Environment environment, String chainName, int index) {
    final String keepAliveDuration =
        getProperty(
            environment, buildNodeAttribute(KEEP_ALIVE_DURATION_ATTRIBUTTE, chainName, index));

    if (keepAliveDuration == null) {
      return DEFAULT_KEEP_ALIVE_DURATION;
    }

    return Long.valueOf(keepAliveDuration);
  }

  private Long getConnectionTimeoutProperty(Environment environment, String chainName, int index) {
    final String connectionTimeout =
        getProperty(
            environment, buildNodeAttribute(CONNECTION_TIMEOUT_ATTRIBUTE, chainName, index));

    if (connectionTimeout == null) {
      return DEFAULT_CONNECTION_TIMEOUT;
    }

    return Long.valueOf(connectionTimeout);
  }

  private Long getReadTimeoutProperty(Environment environment, String chainName, int index) {
    final String readTimeout =
        getProperty(environment, buildNodeAttribute(READ_TIMEOUT_ATTRIBUTTE, chainName, index));

    if (readTimeout == null) {
      return DEFAULT_READ_TIMEOUT;
    }

    return Long.valueOf(readTimeout);
  }

  private Integer getSyncingThresholdProperty(
      Environment environment, String chainName, int index) {
    final String syncingThreshold =
        getProperty(environment, buildNodeAttribute(SYNCING_THRESHOLD_ATTRIBUTE, chainName, index));

    if (syncingThreshold == null) {
      return DEFAULT_SYNCING_THRESHOLD;
    }

    return Integer.valueOf(syncingThreshold);
  }

  private BigInteger getBlocksToWaitForConfirmationProperty(
      Environment environment, String chainName, int index) {
    String blocksToWaitForConfirmation =
        getProperty(
            environment,
            buildNodeAttribute(BLOCKS_TO_WAIT_FOR_CONFIRMATION_ATTRIBUTE, chainName, index));

    if (blocksToWaitForConfirmation == null) {
      blocksToWaitForConfirmation =
          getProperty(
              environment,
              GLOBAL_BLOCKS_TO_WAIT_FOR_CONFIRMATION_ATTRIBUTE,
              DEFAULT_BLOCKS_TO_WAIT_FOR_CONFIRMATION);
    }

    return BigInteger.valueOf(Long.parseLong(blocksToWaitForConfirmation));
  }

  private BigInteger getBlocksToWaitBeforeInvalidatingProperty(
      Environment environment, String chainName, int index) {
    String blocksToWaitBeforeInvalidating =
        getProperty(
            environment,
            buildNodeAttribute(BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTRIBUTE, chainName, index));

    if (blocksToWaitBeforeInvalidating == null) {
      blocksToWaitBeforeInvalidating =
          getProperty(
              environment,
              GLOBAL_BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTIBUTE,
              DEFAULT_BLOCKS_TO_WAIT_BEFORE_INVALIDATING);
    }

    return BigInteger.valueOf(Long.parseLong(blocksToWaitBeforeInvalidating));
  }

  private BigInteger getBlocksToWaitForMissingTxProperty(
      Environment environment, String chainName, int index) {
    String blocksToWaitForMissingTx =
        getProperty(
            environment,
            buildNodeAttribute(BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE, chainName, index));

    if (blocksToWaitForMissingTx == null) {
      blocksToWaitForMissingTx =
          getProperty(
              environment,
              GLOBAL_BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE,
              DEFAULT_BLOCKS_TO_WAIT_FOR_MISSING_TX);
    }

    return BigInteger.valueOf(Long.parseLong(blocksToWaitForMissingTx));
  }

  private BigInteger getInitialStartBlockProperty(
      Environment environment, String chainName, int index) {
    String initialStartBlock =
        getProperty(
            environment, buildNodeAttribute(NODE_SYNC_START_BLOCK_ATTRIBUTE, chainName, index));
    if (initialStartBlock != null) {
      logger.warn(
          "Use property INITIAL_START_BLOCK instead of NODE_SYNC_START_BLOCK because has been deprecrated.");
    } else {
      initialStartBlock =
          getProperty(
              environment, buildNodeAttribute(INITIAL_START_BLOCK_ATTRIBUTE, chainName, index));
    }
    if (initialStartBlock == null) {
      initialStartBlock =
          getProperty(environment, chainName + GLOBAL_INITIAL_START_BLOCK_ATTRIBUTE);
    }

    return initialStartBlock == null
        ? BigInteger.valueOf(Long.parseLong(DEFAULT_SYNC_START_BLOCK))
        : BigInteger.valueOf(Long.parseLong(initialStartBlock));
  }

  private BigInteger getNumBlocksToReplayProperty(
      Environment environment, String chainName, int index) {
    String numBlocksToReplay =
        getProperty(
            environment, buildNodeAttribute(NUM_BLOCKS_TO_REPLAY_ATTRIBUTE, chainName, index));

    if (numBlocksToReplay == null) {
      numBlocksToReplay =
          getProperty(
              environment,
              chainName + GLOBAL_NUM_BLOCKS_TO_REPLAY_ATTRIBUTE,
              DEFAULT_NUM_BLOCKS_TO_REPLAY);
    }

    return BigInteger.valueOf(Long.parseLong(numBlocksToReplay));
  }

  private BigInteger getMaxBlocksToSyncProperty(
      Environment environment, String chainName, int index) {
    String maxBlocksToSync =
        getProperty(
            environment,
            buildNodeAttribute(NODE_MAX_UNSYNCED_BLOCKS_FOR_FILTER_ATTRIBUTE, chainName, index));
    if (maxBlocksToSync == null) {
      maxBlocksToSync = environment.getProperty(DEFAULT_MAX_UNSYNCED_BLOCKS_FOR_FILTER_ATTRIBUTE);
    }
    if (maxBlocksToSync != null) {
      logger.warn(
          "Use properties MAX_BLOCK_TO_SYNC instead of MAX_UNSYNCED_BLOCKS_FOR_FILTER because has been deprecrated.");
    } else {
      maxBlocksToSync =
          getProperty(
              environment, buildNodeAttribute(MAX_BLOCKS_TO_SYNC_ATTRIBUTE, chainName, index));
    }
    if (maxBlocksToSync == null) {
      maxBlocksToSync =
          getProperty(
              environment,
              chainName + GLOBAL_MAX_BLOCKS_TO_SYNC_ATTRIBUTE,
              DEFAULT_MAX_BLOCKS_TO_SYNC);
    }

    return BigInteger.valueOf(Long.parseLong(maxBlocksToSync));
  }

  private String getNodeUsernameProperty(Environment environment, String chainName, int index) {
    return getProperty(environment, buildNodeAttribute(NODE_USERNAME_ATTRIBUTE, chainName, index));
  }

  private String getNodePasswordProperty(Environment environment, String chainName, int index) {
    return getProperty(environment, buildNodeAttribute(NODE_PASSWORD_ATTRIBUTE, chainName, index));
  }

  private String getNodeBlockStrategyProperty(
      Environment environment, String chainName, int index) {
    return getProperty(environment, buildNodeAttribute(BLOCK_STRATEGY_ATTRIBUTE, chainName, index));
  }

  private Boolean getNodeTransactionRevertReasonProperty(
      Environment environment, String chainName, int index) {
    return Boolean.parseBoolean(
        getProperty(
            environment,
            buildNodeAttribute(TRANSACTION_REVERT_REASON_ATTRIBUTTE, chainName, index)));
  }

  private Long getNodeHealthcheckIntervalProperty(
      Environment environment, String chainName, int index) {
    String healthcheckInterval =
        getProperty(
            environment, buildNodeAttribute(NODE_HEALTHCHECK_INTERVAL_ATTRIBUTE, chainName, index));

    if (healthcheckInterval == null) {
      // Get the generic configuration
      healthcheckInterval = environment.getProperty("ethereum.healthcheck.pollInterval");
    }

    if (healthcheckInterval == null) {
      // Get the default configuration
      return DEFAULT_HEALTHCHECK_POLLING_INTERVAL;
    }

    return Long.valueOf(healthcheckInterval);
  }

  @Deprecated
  private BigInteger getMaxUnsyncedBlocksForFilter(
      Environment environment, String chainName, int index) {
    return getMaxBlocksToSyncProperty(environment, chainName, index);
  }

  @Deprecated
  private BigInteger getSyncStartBlock(Environment environment, String chainName, int index) {
    return getInitialStartBlockProperty(environment, chainName, index);
  }

  private String getProperty(Environment environment, String property) {
    return environment.getProperty(property);
  }

  private String getProperty(Environment environment, String property, String defaultValue) {
    return environment.getProperty(property, defaultValue);
  }

  private String buildNodeAttribute(String attribute, String chainName, int index) {
    return new StringBuilder(String.format(chainName + NODE_ATTRIBUTE_PREFIX, index))
        .append(".")
        .append(attribute)
        .toString();
  }
}
