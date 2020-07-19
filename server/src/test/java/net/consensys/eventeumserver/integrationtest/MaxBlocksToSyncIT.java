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

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

@TestPropertySource(locations="classpath:application-test-db.properties",
        properties = {"ethereum.nodes[0].maxBlocksToSync=4"})
public class MaxBlocksToSyncIT extends ServiceRestartRecoveryTests {

    @Test
    public void onlySyncMaxBlocksOnStartup() throws Exception {
        triggerBlocks(1);
        waitForBlockMessages(1);
        waitForFilterPoll();

        final BigInteger lastBlockNumber = getBroadcastBlockMessages()
                .get(getBroadcastBlockMessages().size() - 1).getNumber();

        getBroadcastBlockMessages().clear();

        restartEventeum(() -> {
            try {
                triggerBlocks(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        waitForBlockMessages(4);

        waitForFilterPoll();

        assertEquals(4, getBroadcastBlockMessages().size());

        assertEquals(lastBlockNumber.add(BigInteger.valueOf(7)), getBroadcastBlockMessages().get(0).getNumber());
    }
}
