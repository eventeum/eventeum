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
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.eventstore.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.web3j.crypto.Keys;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {"broadcaster.bytesToAscii=true"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-db.properties")
public class AsciiBytesIT extends MainBroadcasterTests {

  @Autowired private EventStore eventStore;

  @Test
  public void testAsciiBytes() throws Exception {
    final EventEmitter emitter = deployEventEmitterContract();

    final ContractEventFilter registeredFilter =
        registerDummyEventFilter(emitter.getContractAddress());
    emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();

    waitForContractEventMessages(1);

    assertEquals(1, getBroadcastContractEvents().size());

    final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);

    verifyDummyEventDetails(
        registeredFilter,
        eventDetails,
        ContractEventStatus.UNCONFIRMED,
        "BytesValue",
        Keys.toChecksumAddress(CREDS.getAddress()),
        BigInteger.TEN,
        "StringValue");
  }
}
