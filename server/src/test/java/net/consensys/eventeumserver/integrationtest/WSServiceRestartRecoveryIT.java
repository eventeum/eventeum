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

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = "classpath:application-test-ws-pubsub.properties")
public class WSServiceRestartRecoveryIT extends ServiceRestartRecoveryTests {

    @Test
    public void broadcastMissedBlocksOnStartupAfterFailureTest() throws Exception {
        doBroadcastMissedBlocksOnStartupAfterFailureTest();
    }

    @Test
    public void broadcastUnconfirmedEventAfterFailureTest() throws Exception {
        doBroadcastUnconfirmedEventAfterFailureTest();
    }

    @Test
    public void broadcastConfirmedEventAfter12BlocksWhenDownTest() throws Exception {
        doBroadcastConfirmedEventAfter12BlocksWhenDownTest();
    }

    @Test
    public void broadcastTransactionUnconfirmedAfterFailureTest() throws Exception {
        doBroadcastTransactionUnconfirmedAfterFailureTest();
    }
}
