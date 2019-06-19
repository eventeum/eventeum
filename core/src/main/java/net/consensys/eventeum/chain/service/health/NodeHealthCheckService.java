package net.consensys.eventeum.chain.service.health;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.health.strategy.ReconnectionStrategy;
import net.consensys.eventeum.service.SubscriptionService;
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

    private ReconnectionStrategy reconnectionStrategy;

    private SubscriptionService subscriptionService;
  
    private boolean initiallySubscribed = false;

    public NodeHealthCheckService(BlockchainService blockchainService,
                                  ReconnectionStrategy reconnectionStrategy,
                                  SubscriptionService subscriptionService) {
        this.blockchainService = blockchainService;
        this.reconnectionStrategy = reconnectionStrategy;
        this.subscriptionService = subscriptionService;
        nodeStatus = NodeStatus.SUBSCRIBED;
    }

    @Scheduled(fixedDelayString = "${ethereum.healthcheck.pollInterval}")
    public void checkHealth() {

        //Can take a few seconds to subscribe initially so if wait until after
        //first subscription to check health
        if (!isSubscribed() && !initiallySubscribed) {
            return;
        }

        final NodeStatus statusAtStart = nodeStatus;

        if (isNodeConnected()) {
            if (nodeStatus == NodeStatus.DOWN) {
                log.info("Node {} has come back up.", blockchainService.getNodeName());

                //We've come back up
                doResubscribe();
            } else {
                if (statusAtStart != NodeStatus.SUBSCRIBED || !isSubscribed()) {
                    log.info("Node {} not subscribed", blockchainService.getNodeName());
                    doResubscribe();
                } else {
                    initiallySubscribed = true;
                }
            }

        } else {
            log.error("Node {} is down!!", blockchainService.getNodeName());
            nodeStatus = NodeStatus.DOWN;

            if (statusAtStart != NodeStatus.DOWN) {
                subscriptionService.unsubscribeToAllSubscriptions();
            }

            doReconnect();
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

    private void doReconnect() {
        reconnectionStrategy.reconnect();

        if (isNodeConnected()) {
            nodeStatus = NodeStatus.CONNECTED;
            doResubscribe();
        }
    }

    private void doResubscribe() {
        reconnectionStrategy.resubscribe();

        nodeStatus = isSubscribed() ? NodeStatus.SUBSCRIBED : NodeStatus.CONNECTED;
    }

    private enum NodeStatus {
        CONNECTED,
        SUBSCRIBED,
        DOWN
    }

}
