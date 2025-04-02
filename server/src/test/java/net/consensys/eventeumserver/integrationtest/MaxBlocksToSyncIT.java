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

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.constant.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
    locations = "classpath:application-test-db.properties",
    properties = {"ethereum.nodes[0].maxBlocksToSync=4"})
public class MaxBlocksToSyncIT extends ServiceRestartRecoveryTests {

  @Autowired @Lazy ChainServicesContainer chainServices;

  @Test
  public void onlySyncMaxBlocksOnStartup() throws Exception {
    triggerBlocks(1);
    waitForBlockMessages(1);
    waitForFilterPoll();

    TestContextManager tc = new TestContextManager(getClass());
    tc.prepareTestInstance(this);

    AtomicReference<BigInteger> lastBlockNumber = new AtomicReference<>(BigInteger.ZERO);

    restartEventeumKafka(
        () -> {
          lastBlockNumber.set(
              getBroadcastBlockMessages().get(getBroadcastBlockMessages().size() - 1).getNumber());
          getBroadcastBlockMessages().clear();
        },
        tc);

    waitForBlockMessages(4);

    waitForFilterPoll();

    assertEquals(4, getBroadcastBlockMessages().size());

    // Current number at start - 4 (3) of maxSync
    assertEquals(
        chainServices
            .getNodeServices(Constants.DEFAULT_NODE_NAME)
            .getBlockchainService()
            .getCurrentBlockNumber()
            .subtract(BigInteger.valueOf(3)),
        getBroadcastBlockMessages().get(0).getNumber());
  }
}
