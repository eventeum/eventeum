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
import java.util.UUID;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

public class NodeRecoveryTests extends BaseKafkaIntegrationTest {

  protected void doSingleNodeFailureRecoveryTest() throws Exception {
    final EventEmitter emitter = deployEventEmitterContract();

    final ContractEventFilter registeredFilter =
        registerDummyEventFilter(emitter.getContractAddress());

    Thread.sleep(1000);

    doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 4000);
  }

  protected void doMultipleNodeFailuresRecoveryTest() throws Exception {
    final EventEmitter emitter = deployEventEmitterContract();

    final ContractEventFilter registeredFilter =
        registerDummyEventFilter(emitter.getContractAddress());

    Thread.sleep(1000);

    doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 4000);
    doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 6000);
    doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 8000);
    doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 10000);
    doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 20000);
  }

  protected void doQuickSuccessionNodeFailuresRecoveryTest() throws Exception {
    final EventEmitter emitter = deployEventEmitterContract();

    final ContractEventFilter registeredFilter =
        registerDummyEventFilter(emitter.getContractAddress());

    Thread.sleep(1000);

    final int numRestarts = 20;

    for (int i = 0; i < numRestarts; i++) {
      restartParity(300);
    }

    emitEventAndVerify(emitter, registeredFilter);
  }

  private void doParityRestartEventEmissionsAssertion(
      EventEmitter emitter, ContractEventFilter registeredFilter, long timeToRecovery)
      throws Exception {
    getBroadcastContractEvents().clear();

    restartParity(timeToRecovery);

    emitEventAndVerify(emitter, registeredFilter);
  }

  private void emitEventAndVerify(EventEmitter emitter, ContractEventFilter registeredFilter)
      throws Exception {
    final String valueOne = UUID.randomUUID().toString().substring(0, 10);
    final String valueFour = UUID.randomUUID().toString().substring(0, 10);
    emitter.emitEvent(stringToBytes(valueOne), BigInteger.valueOf(123), valueFour).send();

    waitForContractEventMessages(1);

    assertEquals(1, getBroadcastContractEvents().size());

    final ContractEventDetails secondEventDetails = getBroadcastContractEvents().get(0);

    verifyDummyEventDetails(
        registeredFilter,
        secondEventDetails,
        ContractEventStatus.UNCONFIRMED,
        toHexWithTrailingZeros(valueOne.getBytes(), 66),
        Keys.toChecksumAddress(CREDS.getAddress()),
        BigInteger.valueOf(123),
        valueFour);
  }

  private String toHexWithTrailingZeros(byte[] bytes, int length) {
    String hex = Numeric.toHexString(bytes);

    while (hex.length() != length) {
      hex = hex + "0";
    }

    return hex;
  }

  private void restartParity(long timeToRecovery) throws Exception {
    stopParity();

    Thread.sleep(timeToRecovery);

    startParity();
  }
}
