package net.consensys.eventeum.plugin.chain.block.tx;

import java.util.List;
import java.util.Optional;
import net.consensys.eventeum.chain.block.tx.DefaultTransactionMonitoringBlockListener;
import net.consensys.eventeum.chain.block.tx.criteria.TransactionMatchingCriteria;
import net.consensys.eventeum.chain.factory.TransactionDetailsFactory;
import net.consensys.eventeum.chain.service.block.BlockCache;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.settings.NodeSettings;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.plugin.chain.model.PrivacyConfNode;
import net.consensys.eventeum.plugin.chain.service.Web3JEeaService;
import net.consensys.eventeum.plugin.chain.util.PrivacyUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class EeaTransactionMonitoringBlockListener
    extends DefaultTransactionMonitoringBlockListener {

  private final NodeSettings nodeSettings;
  private final ChainServicesContainer chainServicesContainer;
  private final TransactionDetailsFactory transactionDetailsFactory;

  public EeaTransactionMonitoringBlockListener(
      ChainServicesContainer chainServicesContainer,
      BlockchainEventBroadcaster broadcaster,
      TransactionDetailsFactory transactionDetailsFactory,
      BlockCache blockCache,
      NodeSettings nodeSettings) {
    super(chainServicesContainer, broadcaster, transactionDetailsFactory, blockCache, nodeSettings);
    this.nodeSettings = nodeSettings;
    this.chainServicesContainer = chainServicesContainer;
    this.transactionDetailsFactory = transactionDetailsFactory;
  }

  @Override
  protected void broadcastIfMatched(
      Transaction tx, Block block, List<TransactionMatchingCriteria> criteriaToCheck) {
    Node node = nodeSettings.getNode(block.getNodeName());
    PrivacyConfNode privacyConf =
        PrivacyUtils.buildPrivacyConfNodeFromExtension(node.getExtension());

    // If node has privacy enabled
    if (privacyConf.isEnabled()) {
      Web3JEeaService eeaInstance =
          (Web3JEeaService)
              chainServicesContainer.getNodeServices(block.getNodeName()).getBlockchainService();
      if (eeaInstance.isPrivateTransaction(tx)) {
        Optional.ofNullable(eeaInstance.getPrivateTransactionReceipt(tx.getHash()))
            .ifPresent(
                txReceipt -> {
                  final TransactionDetails txDetails =
                      transactionDetailsFactory.createTransactionDetails(
                          tx,
                          txReceipt.getStatus().equals("0x1")
                              ? TransactionStatus.CONFIRMED
                              : TransactionStatus.FAILED,
                          block);
                  txDetails.setTo(txReceipt.getTo());
                  txDetails.setRevertReason(
                      node.getAddTransactionRevertReason() ? txReceipt.getRevertReason() : null);
                  criteriaToCheck.stream()
                      .filter(matcher -> matcher.isAMatch(txDetails))
                      .findFirst()
                      .ifPresent(matcher -> onTransactionMatched(txDetails, matcher));
                });
        return;
      }
    }
    super.broadcastIfMatched(tx, block, criteriaToCheck);
  }
}
