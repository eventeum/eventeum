package net.consensys.eventeum.chain.settings;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Data
@Component
public class NodeSettings {

    private static final Long DEFAULT_POLLING_INTERVAL = 10000l;

    private static final String ATTRIBUTE_PREFIX = "ethereum";

    private static final String NODE_ATTRIBUTE_PREFIX = ATTRIBUTE_PREFIX + ".nodes[%s]";

    private static final String NODE_URL_ATTRIBUTE = "url";

    private static final String NODE_NAME_ATTRIBUTE = "name";

    private static final String NODE_USERNAME_ATTRIBUTE = "username";

    private static final String NODE_PASSWORD_ATTRIBUTE = "password";

    private static final String NODE_POLLING_INTERVAL_ATTRIBUTE = "pollingInterval";

    private static final String BLOCK_STRATEGY_ATTRIBUTE = "blockStrategy";

    private static final String TRANSACTION_REVERT_REASON = "addTransactionRevertReason";

    private Map<String, Node> nodes;

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
                    getNodeTransactionRevertReasonProperty(environment, index)
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
        return Boolean.parseBoolean(getProperty(environment, buildNodeAttribute(TRANSACTION_REVERT_REASON, index)));
    }

    private String getProperty(Environment environment, String property) {
        return environment.getProperty(property);
    }

    private String buildNodeAttribute(String attribute, int index) {
        return new StringBuilder(String.format(NODE_ATTRIBUTE_PREFIX, index))
                .append(".")
                .append(attribute)
                .toString();
    }
}
