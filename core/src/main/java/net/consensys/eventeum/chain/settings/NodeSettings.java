package net.consensys.eventeum.chain.settings;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainException;
import org.springframework.core.env.Environment;

@Data
public class NodeSettings {

    private static final String ATTRIBUTE_PREFIX = "ethereum";

    private static final String NODE_ATTRIBUTE_PREFIX = ATTRIBUTE_PREFIX + ".nodes[%s]";

    private static final String NODE_URL_ATTRIBUTE = "url";

    private static final String NODE_NAME_ATTRIBUTE = "name";

    private static final String NODE_USERNAME_ATTRIBUTE = "username";

    private static final String NODE_PASSWORD_ATTRIBUTE = "password";

    private static final String BLOCK_STRATEGY_ATTRIBUTE = "blockStrategy";

    private List<Node> nodes;

    private String blockStrategy;

    public NodeSettings(Environment environment) {
        populateNodeSettings(environment);

        blockStrategy = environment.getProperty(ATTRIBUTE_PREFIX + "." + BLOCK_STRATEGY_ATTRIBUTE);
    }

    private void populateNodeSettings(Environment environment) {
        nodes = new ArrayList<>();

        int index = 0;

        while (nodeExistsAtIndex(environment, index)) {
            nodes.add(new Node(
                    getNodeNameProperty(environment, index),
                    getNodeUrlProperty(environment, index),
                    getNodeUsernameProperty(environment, index),
                    getNodePasswordProperty(environment, index)));

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

    private String getNodeUsernameProperty(Environment environment, int index) {
        return getProperty(environment, buildNodeAttribute(NODE_USERNAME_ATTRIBUTE, index));
    }

    private String getNodePasswordProperty(Environment environment, int index) {
        return getProperty(environment, buildNodeAttribute(NODE_PASSWORD_ATTRIBUTE, index));
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
