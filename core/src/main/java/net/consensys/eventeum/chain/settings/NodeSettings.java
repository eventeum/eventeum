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
import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Data
@Component
public class NodeSettings {

    private static final Long DEFAULT_POLLING_INTERVAL = 10000l;

    private static final Long DEFAULT_HEALTHCHECK_POLLING_INTERVAL = 10000l;

    private static final Long DEFAULT_KEEP_ALIVE_DURATION = 10000l;

    private static final Integer DEFAULT_MAX_IDLE_CONNECTIONS = 5;

    private static final Long DEFAULT_CONNECTION_TIMEOUT = 5000l;

    private static final Integer DEFAULT_SYNCING_THRESHOLD = 60;

    private static final Long DEFAULT_READ_TIMEOUT = 60000l;

    private static final String DEFAULT_BLOCKS_TO_WAIT_FOR_MISSING_TX = "200";

    private static final String DEFAULT_BLOCKS_TO_WAIT_BEFORE_INVALIDATING = "2";

    private static final String DEFAULT_BLOCKS_TO_WAIT_FOR_CONFIRMATION = "12";

    private static final String DEFAULT_NUM_BLOCKS_TO_REPLAY = "12";

    private static final String DEFAULT_MAX_BLOCKS_TO_SYNC = "0";

    private static final String ATTRIBUTE_PREFIX = "ethereum";

    private static final String NODE_ATTRIBUTE_PREFIX = ATTRIBUTE_PREFIX + ".nodes[%s]";

    private static final String NODE_URL_ATTRIBUTE = "url";

    private static final String NODE_NAME_ATTRIBUTE = "name";

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

    private static final String GLOBAL_BLOCKS_TO_WAIT_FOR_CONFIRMATION_ATTRIBUTE = "broadcaster.event.confirmation.numBlocksToWait";

    private static final String BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTRIBUTE = "numBlocksToWaitBeforeInvalidating";

    private static final String GLOBAL_BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTIBUTE = "broadcaster.event.confirmation.numBlocksToWaitBeforeInvalidating";

    private static final String BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE = "numBlocksToWaitForMissingTx";

    private static final String GLOBAL_BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE = "broadcaster.event.confirmation.numBlocksToWaitForMissingTx";

    private static final String INITIAL_START_BLOCK_ATTRIBUTE = "initialStartBlock";

    private static final String GLOBAL_INITIAL_START_BLOCK_ATTRIBUTE = ATTRIBUTE_PREFIX + "." + INITIAL_START_BLOCK_ATTRIBUTE;

    private static final String NUM_BLOCKS_TO_REPLAY_ATTRIBUTE = "numBlocksToReplay";

    private static final String GLOBAL_NUM_BLOCKS_TO_REPLAY_ATTRIBUTE = ATTRIBUTE_PREFIX + "." + NUM_BLOCKS_TO_REPLAY_ATTRIBUTE;

    private static final String MAX_BLOCKS_TO_SYNC_ATTRIBUTE = "maxBlocksToSync";

    private static final String GLOBAL_MAX_BLOCKS_TO_SYNC_ATTRIBUTE = ATTRIBUTE_PREFIX + "." + MAX_BLOCKS_TO_SYNC_ATTRIBUTE;

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
        nodes = new HashMap <String, Node>();
        int index = 0;

        while (nodeExistsAtIndex(environment, index)) {
            String nodeName = getNodeNameProperty(environment, index);
            Node node = new Node(
                    nodeName,
                    getNodeUrlProperty(environment, index),
                    getNodePollingIntervalProperty(environment, index),
                    getNodeUsernameProperty(environment, index),
                    getNodePasswordProperty(environment, index),
                    getNodeBlockStrategyProperty(environment, index),
                    getNodeTransactionRevertReasonProperty(environment, index),
                    getMaxIdleConnectionsProperty(environment, index),
                    getKeepAliveDurationProperty(environment, index),
                    getConnectionTimeoutProperty(environment, index),
                    getReadTimeoutProperty(environment, index),
                    getSyncingThresholdProperty(environment,index),
                    getNodeHealthcheckIntervalProperty(environment, index),
                    getBlocksToWaitForConfirmationProperty(environment, index),
                    getBlocksToWaitBeforeInvalidatingProperty(environment, index),
                    getBlocksToWaitForMissingTxProperty(environment, index),
                    getInitialStartBlockProperty(environment, index),
                    getNumBlocksToReplayProperty(environment, index),
                    getMaxBlocksToSyncProperty(environment, index)
            );

            nodes.put(nodeName, node);

            index++;
        }

        if (nodes.isEmpty()) {
            throw new BlockchainException("No nodes configured!");
        }
    }

    private boolean nodeExistsAtIndex(Environment environment, int index) {
        return environment.containsProperty(buildNodeAttribute(NODE_NAME_ATTRIBUTE, index));
    }

    private String getNodeNameProperty(Environment environment, int index) {
        return getProperty(environment, buildNodeAttribute(NODE_NAME_ATTRIBUTE, index));
    }

    private String getNodeUrlProperty(Environment environment, int index) {
        return getProperty(environment, buildNodeAttribute(NODE_URL_ATTRIBUTE, index));
    }

    private Long getNodePollingIntervalProperty(Environment environment, int index) {
        final String pollingInterval =
                getProperty(environment, buildNodeAttribute(NODE_POLLING_INTERVAL_ATTRIBUTE, index));

        if (pollingInterval == null) {
            return DEFAULT_POLLING_INTERVAL;
        }

        return Long.valueOf(pollingInterval);
    }

    private Integer getMaxIdleConnectionsProperty(Environment environment, int index) {
        final String maxIdleConnections =
                getProperty(environment, buildNodeAttribute(MAX_IDLE_CONNECTIONS_ATTRIBUTTE, index));

        if (maxIdleConnections == null) {
            return DEFAULT_MAX_IDLE_CONNECTIONS;
        }

        return Integer.valueOf(maxIdleConnections);
    }

    private Long getKeepAliveDurationProperty(Environment environment, int index) {
        final String keepAliveDuration =
                getProperty(environment, buildNodeAttribute(KEEP_ALIVE_DURATION_ATTRIBUTTE, index));

        if (keepAliveDuration == null) {
            return DEFAULT_KEEP_ALIVE_DURATION;
        }

        return Long.valueOf(keepAliveDuration);
    }

    private Long getConnectionTimeoutProperty(Environment environment, int index) {
        final String connectionTimeout =
                getProperty(environment, buildNodeAttribute(CONNECTION_TIMEOUT_ATTRIBUTE, index));

        if (connectionTimeout == null) {
            return DEFAULT_CONNECTION_TIMEOUT;
        }

        return Long.valueOf(connectionTimeout);
    }

    private Long getReadTimeoutProperty(Environment environment, int index) {
        final String readTimeout =
                getProperty(environment, buildNodeAttribute(READ_TIMEOUT_ATTRIBUTTE, index));

        if (readTimeout == null) {
            return DEFAULT_READ_TIMEOUT;
        }

        return Long.valueOf(readTimeout);
    }

    private Integer getSyncingThresholdProperty(Environment environment, int index) {
        final String syncingThreshold =
                getProperty(environment, buildNodeAttribute(SYNCING_THRESHOLD_ATTRIBUTE, index));

        if (syncingThreshold == null) {
            return DEFAULT_SYNCING_THRESHOLD;
        }

        return Integer.valueOf(syncingThreshold);
    }

    private BigInteger getBlocksToWaitForConfirmationProperty(Environment environment, int index) {
        String blocksToWaitForConfirmation =
                getProperty(environment, buildNodeAttribute(BLOCKS_TO_WAIT_FOR_CONFIRMATION_ATTRIBUTE, index));

        if (blocksToWaitForConfirmation == null) {
            blocksToWaitForConfirmation = getProperty(environment,
                    GLOBAL_BLOCKS_TO_WAIT_FOR_CONFIRMATION_ATTRIBUTE, DEFAULT_BLOCKS_TO_WAIT_FOR_CONFIRMATION);
        }

        return BigInteger.valueOf(Long.valueOf(blocksToWaitForConfirmation));
    }

    private BigInteger getBlocksToWaitBeforeInvalidatingProperty(Environment environment, int index) {
        String blocksToWaitBeforeInvalidating =
                getProperty(environment, buildNodeAttribute(BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTRIBUTE, index));

        if (blocksToWaitBeforeInvalidating == null) {
            blocksToWaitBeforeInvalidating = getProperty(environment,
                    GLOBAL_BLOCKS_TO_WAIT_BEFORE_INVALIDATING_ATTIBUTE, DEFAULT_BLOCKS_TO_WAIT_BEFORE_INVALIDATING);
        }

        return BigInteger.valueOf(Long.valueOf(blocksToWaitBeforeInvalidating));
    }

    private BigInteger getBlocksToWaitForMissingTxProperty(Environment environment, int index) {
        String blocksToWaitForMissingTx =
                getProperty(environment, buildNodeAttribute(BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE, index));

        if (blocksToWaitForMissingTx == null) {
            blocksToWaitForMissingTx = getProperty(environment,
                    GLOBAL_BLOCKS_TO_WAIT_FOR_MISSING_TX_ATTRIBUTE, DEFAULT_BLOCKS_TO_WAIT_FOR_MISSING_TX);
        }

        return BigInteger.valueOf(Long.valueOf(blocksToWaitForMissingTx));
    }

    private BigInteger getInitialStartBlockProperty(Environment environment, int index) {
        String initialStartBlock =
                getProperty(environment, buildNodeAttribute(INITIAL_START_BLOCK_ATTRIBUTE, index));

        if (initialStartBlock == null) {
            initialStartBlock = getProperty(environment, GLOBAL_INITIAL_START_BLOCK_ATTRIBUTE);
        }

        return initialStartBlock == null ? null : BigInteger.valueOf(Long.valueOf(initialStartBlock));
    }

    private BigInteger getNumBlocksToReplayProperty(Environment environment, int index) {
        String numBlocksToReplay =
                getProperty(environment, buildNodeAttribute(NUM_BLOCKS_TO_REPLAY_ATTRIBUTE, index));

        if (numBlocksToReplay == null) {
            numBlocksToReplay = getProperty(environment,
                    GLOBAL_NUM_BLOCKS_TO_REPLAY_ATTRIBUTE, DEFAULT_NUM_BLOCKS_TO_REPLAY);
        }

        return BigInteger.valueOf(Long.valueOf(numBlocksToReplay));
    }

    private BigInteger getMaxBlocksToSyncProperty(Environment environment, int index) {
        String maxBlocksToSync =
                getProperty(environment, buildNodeAttribute(MAX_BLOCKS_TO_SYNC_ATTRIBUTE, index));

        if (maxBlocksToSync == null) {
            maxBlocksToSync = getProperty(environment,
                    GLOBAL_MAX_BLOCKS_TO_SYNC_ATTRIBUTE, DEFAULT_MAX_BLOCKS_TO_SYNC);
        }

        return BigInteger.valueOf(Long.valueOf(maxBlocksToSync));
    }

    private String getNodeUsernameProperty(Environment environment, int index) {
        return getProperty(environment, buildNodeAttribute(NODE_USERNAME_ATTRIBUTE, index));
    }

    private String getNodePasswordProperty(Environment environment, int index) {
        return getProperty(environment, buildNodeAttribute(NODE_PASSWORD_ATTRIBUTE, index));
    }

    private String getNodeBlockStrategyProperty(Environment environment, int index) {
        return getProperty(environment, buildNodeAttribute(BLOCK_STRATEGY_ATTRIBUTE, index));
    }

    private Boolean getNodeTransactionRevertReasonProperty(Environment environment, int index) {
        return Boolean.parseBoolean(getProperty(environment, buildNodeAttribute(TRANSACTION_REVERT_REASON_ATTRIBUTTE, index)));
    }

    private Long getNodeHealthcheckIntervalProperty(Environment environment, int index) {
        String healthcheckInterval =
                getProperty(environment, buildNodeAttribute(NODE_HEALTHCHECK_INTERVAL_ATTRIBUTE, index));

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

    private String getProperty(Environment environment, String property) {
        return environment.getProperty(property);
    }

    private String getProperty(Environment environment, String property, String defaultValue) {
        return environment.getProperty(property, defaultValue);
    }

    private String buildNodeAttribute(String attribute, int index) {
        return new StringBuilder(String.format(NODE_ATTRIBUTE_PREFIX, index))
                .append(".")
                .append(attribute)
                .toString();
    }
}
