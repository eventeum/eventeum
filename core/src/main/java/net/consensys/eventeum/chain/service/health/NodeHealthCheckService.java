package net.consensys.eventeum.chain.service.health;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.listener.NodeFailureListener;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * A service that constantly polls an ethereum node (getClientVersion) in order to ensure that the node
 * is currently running.  If a failure is detected, each configured NodeFailureListener is invoked.
 * This is also the case when it is detected that a node has recovered after failure.
 *
 * The poll interval can be configured with the ethereum.node.healthcheck.pollInterval property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
public class NodeHealthCheckService {

    private BlockchainService blockchainService;

    private NodeStatus nodeStatus;

    private NodeFailureListener failureListener;

    public NodeHealthCheckService(BlockchainService blockchainService,
                                  NodeFailureListener failureListener) {
        this.blockchainService = blockchainService;
        this.failureListener = failureListener;
        nodeStatus = NodeStatus.SUBSCRIBED;
    }

    @Scheduled(fixedDelayString = "${ethereum.healthcheck.pollInterval}")
    public void checkHealth() {
        final NodeStatus statusAtStart = nodeStatus;

        if (isNodeConnected()) {
            if (nodeStatus == NodeStatus.DOWN) {
                log.info("Node {} has come back up.", blockchainService.getNodeName());

                //We've come back up
                failureListener.onNodeRecovery();
                nodeStatus = NodeStatus.CONNECTED;
            }

            if (isSubscribed()) {
                //We weren't previously subscribed, but we are now!
                if (statusAtStart != NodeStatus.SUBSCRIBED) {
                    failureListener.onNodeSubscribed();
                }

                nodeStatus = NodeStatus.SUBSCRIBED;
            } else if (statusAtStart == NodeStatus.SUBSCRIBED) {
                //We were previously subscribed, but not any longer
                log.info("Node {} subscriptions have been lost, attempting to resubscribe", blockchainService.getNodeName());
                failureListener.onNodeRecovery();
                nodeStatus = NodeStatus.CONNECTED;
            }
        } else {

            if (nodeStatus != NodeStatus.DOWN) {
                log.error("Node {} is down!!", blockchainService.getNodeName());
                //First sign of failure
                failureListener.onNodeFailure();
            } else {
                log.error("Node {} is still down!!", blockchainService.getNodeName());
            }
            nodeStatus = NodeStatus.DOWN;
        }
    }

    protected boolean isNodeConnected() {
        try {
            blockchainService.getClientVersion();
        } catch(Throwable t) {
            log.error("Get client version failed with exception on node " + blockchainService.getNodeName(), t);

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
