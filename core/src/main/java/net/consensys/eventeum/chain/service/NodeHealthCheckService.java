package net.consensys.eventeum.chain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
@Component
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
        nodeStatus = NodeStatus.RUNNING;
    }

    @Scheduled(fixedDelayString = "${ethereum.node.healthcheck.pollInterval}")
    public void checkHealth() {
        try {
            blockchainService.getClientVersion();

            if (nodeStatus == NodeStatus.DOWN) {
                LOGGER.info("Node has come back up.");
                //We've come back up
                failureListeners.forEach((listener) -> listener.onNodeRecovery());
            }
            nodeStatus = NodeStatus.RUNNING;
        } catch(Throwable t) {
            LOGGER.error("Node is down!!", t);

            if (nodeStatus == NodeStatus.RUNNING) {
                //First sign of failure
                failureListeners.forEach((listener) -> listener.onNodeFailure());
            }
            nodeStatus = NodeStatus.DOWN;
        }
    }

    private enum NodeStatus {
        RUNNING,
        DOWN
    }

}
