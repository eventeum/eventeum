package net.consensys.eventeum.chain.service.health;

import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.listener.NodeFailureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.soap.Node;
import java.util.List;

/**
 * A service that constantly polls an ethereum node (getClientVersion) in order to ensure that the node
 * is currently running.  If a failure is detected, each configured NodeFailureListener is invoked.
 * This is also the case when it is detected that a node has recovered after failure.
 *
 * The poll interval can be configured with the ethereum.node.healthcheck.pollInterval property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Service
public class NodeHealthCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeHealthCheckService.class);

    private BlockchainService blockchainService;

    private NodeStatus nodeStatus;

    private List<NodeFailureListener> failureListeners;

    @Autowired
    public NodeHealthCheckService(BlockchainService blockchainService,
                                  List<NodeFailureListener> failureListeners) {
        this.blockchainService = blockchainService;
        this.failureListeners = failureListeners;
        nodeStatus = NodeStatus.SUBSCRIBED;
    }

    @Scheduled(fixedDelayString = "${ethereum.node.healthcheck.pollInterval}")
    public void checkHealth() {
        final NodeStatus statusAtStart = nodeStatus;

        if (isNodeConnected()) {
            if (nodeStatus == NodeStatus.DOWN) {
                LOGGER.info("Node has come back up.");

                //We've come back up
                failureListeners.forEach((listener) -> listener.onNodeRecovery());
                nodeStatus = NodeStatus.CONNECTED;
            }

            if (isSubscribed()) {
                //We weren't previously subscribed, but we are now!
                if (statusAtStart != NodeStatus.SUBSCRIBED) {
                    failureListeners.forEach((listener) -> listener.onNodeSubscribed());
                }

                nodeStatus = NodeStatus.SUBSCRIBED;
            } else if (statusAtStart == NodeStatus.SUBSCRIBED) {
                //We were previously subscribed, but not any longer
                LOGGER.info("Node subscriptions have been lost, attempting to resubscribe");
                failureListeners.forEach((listener) -> listener.onNodeRecovery());
                nodeStatus = NodeStatus.CONNECTED;
            }
        } else {
            LOGGER.error("Node is down!!");

            if (nodeStatus != NodeStatus.DOWN) {
                //First sign of failure
                failureListeners.forEach((listener) -> listener.onNodeFailure());
            }
            nodeStatus = NodeStatus.DOWN;
        }
    }

    protected boolean isNodeConnected() {
        try {
            blockchainService.getClientVersion();
        } catch(Throwable t) {
            LOGGER.error("Get client version failed with exception", t);

            return false;
        }

        return true;
    }

    protected boolean isSubscribed() {
        return blockchainService.isConnected();
    }

    private enum NodeStatus {
        CONNECTED,
        SUBSCRIBED,
        DOWN
    }

}
