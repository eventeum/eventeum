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

package net.consensys.eventeumserver.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.model.TransactionIdentifierType;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-tx-monitor.properties")
public class TransactionMonitorIT extends MainBroadcasterTests {

  private static final String TO_ADDRESS = "0x607f4c5bb672230e8672085532f7e901544a7375";

  @Test
  public void testMultipleTransactions() throws Exception {
    final String[] hashes = new String[3];

    for (int i = 0; i < 3; i++) {
      hashes[i] = doTestBroadcastsUnconfirmedTransactionAfterInitialMining();
      clearMessages();
      triggerBlocksAndCheckMessagesSize(3, 0);
      triggerBlocks(1);
    }

    waitForConfirmedTransaction(hashes[0], 1);

    triggerBlocksAndCheckMessagesSize(3, 1);
    triggerBlocks(1);
    waitForConfirmedTransaction(hashes[1], 2);

    triggerBlocksAndCheckMessagesSize(3, 2);
    triggerBlocks(1);
    waitForConfirmedTransaction(hashes[2], 3);
  }

  @Test
  public void testLoadFilterFromConfig() throws Exception {
    final String rawTx = createRawSignedTransactionHex(TO_ADDRESS);
    final String txHash = Hash.sha3(rawTx);

    sendRawTransaction(rawTx);

    waitForTransactionMessages(1);

    assertTrue(getBroadcastTransactionMessages().size() > 0);

    final TransactionDetails txDetails =
        getBroadcastTransactionMessages().stream()
            .filter(x -> x.getHash().equals(txHash))
            .findFirst()
            .get();

    assertEquals(txHash, txDetails.getHash());
    assertEquals(TransactionStatus.UNCONFIRMED, txDetails.getStatus());
  }

  @Test
  public void testContractCreationTransactionContainsContractAddress() throws Exception {
    final TransactionMonitoringSpec monitorSpec =
        new TransactionMonitoringSpec(
            TransactionIdentifierType.FROM_ADDRESS,
            CREDS.getAddress(),
            Constants.DEFAULT_NODE_NAME);

    monitorTransaction(monitorSpec);

    final String contractAddress = deployEventEmitterContract().getContractAddress();

    waitForTransactionMessages(2, false);

    TransactionDetails txDetails =
        getBroadcastTransactionMessages().stream()
            .filter(
                x ->
                    x.getTo() == null
                        && Keys.toChecksumAddress(contractAddress).equals(x.getContractAddress()))
            .findFirst()
            .get();

    // assertNull(txDetails.getTo());
    assertEquals(Keys.toChecksumAddress(contractAddress), txDetails.getContractAddress());
  }

  private void waitForConfirmedTransaction(String hash, int expectedNumMessages) {
    waitForTransactionMessages(expectedNumMessages);

    assertTrue(getBroadcastTransactionMessages().size() >= expectedNumMessages);
    TransactionDetails txDetails =
        getBroadcastTransactionMessages().get(getBroadcastTransactionMessages().size() - 1);
    assertEquals(hash, txDetails.getHash());
    assertEquals(TransactionStatus.CONFIRMED, txDetails.getStatus());
  }

  private void triggerBlocksAndCheckMessagesSize(int numBlocks, int expectedNumBroadcasts)
      throws InterruptedException, ExecutionException, IOException {
    for (int i = 0; i < numBlocks; i++) {
      triggerBlocks(1);
      assertTrue(getBroadcastTransactionMessages().size() >= expectedNumBroadcasts);
    }
  }
}
