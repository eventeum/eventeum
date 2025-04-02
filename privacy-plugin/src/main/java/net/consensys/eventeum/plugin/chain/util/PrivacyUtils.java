package net.consensys.eventeum.plugin.chain.util;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.domain.Transaction;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jTransaction;
import net.consensys.eventeum.plugin.chain.model.PrivacyConfFilter;
import net.consensys.eventeum.plugin.chain.model.PrivacyConfNode;
import net.consensys.eventeum.utils.ModelMapperFactory;
import org.modelmapper.ModelMapper;
import org.web3j.protocol.besu.response.privacy.PrivateTransaction;

@Slf4j
public class PrivacyUtils {

  public static final String PRIVACY_EXTENSION_KEY = "privacy";
  public static final String PRIVACY_ENABLED_KEY = "enabled";
  public static final String PRIVACY_GROUP_ID_KEY = "privacyGroupID";
  public static final String PRIVACY_PRECOMPILED_ADDRESS = "privacyPrecompiledAddress";

  public static PrivacyConfNode buildPrivacyConfNodeFromExtension(Map<String, Object> extension) {
    if (extension == null) {
      return null;
    }

    Map<String, Object> subMap = (Map<String, Object>) extension.get(PRIVACY_EXTENSION_KEY);

    return subMap == null
        ? null
        : new PrivacyConfNode(
            Boolean.parseBoolean(subMap.get(PRIVACY_ENABLED_KEY).toString()),
            (String) subMap.get(PRIVACY_PRECOMPILED_ADDRESS));
  }

  public static PrivacyConfFilter buildPrivacyConfFilterFromExtension(
      Map<String, Object> extension) {
    if (extension == null) {
      return null;
    }

    Map<String, Object> subMap = (Map<String, Object>) extension.get(PRIVACY_EXTENSION_KEY);

    return subMap == null
        ? null
        : new PrivacyConfFilter(
            Boolean.parseBoolean(subMap.get(PRIVACY_ENABLED_KEY).toString()),
            (String) subMap.get(PRIVACY_GROUP_ID_KEY));
  }

  public static Transaction buildTransactionFromPrivateTransaction(
      PrivateTransaction privateTransctionReceipt) {
    org.web3j.protocol.core.methods.response.Transaction tx =
        new org.web3j.protocol.core.methods.response.Transaction();
    final ModelMapper modelMapper = ModelMapperFactory.getInstance().getModelMapper();

    modelMapper.map(privateTransctionReceipt, tx);

    return new Web3jTransaction(tx);
  }
}
