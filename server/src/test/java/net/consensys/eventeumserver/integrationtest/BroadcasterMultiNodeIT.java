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
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-multinode.properties")
public class BroadcasterMultiNodeIT extends MainBroadcasterTests {

    //TODO need to add tests that properly run a 2nd node and test events from both nodes

    @Test
    public void testBroadcastsUnconfirmedEventAfterInitialEmit() throws Exception {
        doTestBroadcastsUnconfirmedEventAfterInitialEmit();
    }

    @Test
    public void testBroadcastNotOrderedEvent() throws Exception {
        doTestBroadcastsNotOrderedEvent();
    }

    @Test
    public void testBroadcastsConfirmedEventAfterBlockThresholdReached() throws Exception {
        doTestBroadcastsConfirmedEventAfterBlockThresholdReached();
    }

    @Test
    public void testContractEventForUnregisteredEventFilterNotBroadcast() throws Exception {
        doTestContractEventForUnregisteredEventFilterNotBroadcast();
    }

    @Test
    public void testBroadcastsUnconfirmedTransactionAfterInitialMining() throws Exception {
        doTestBroadcastsUnconfirmedTransactionAfterInitialMining();
    }

    @Test
    public void testBroadcastsConfirmedTransactionAfterBlockThresholdReached() throws Exception {
        doTestBroadcastsConfirmedTransactionAfterBlockThresholdReached();
    }
}
