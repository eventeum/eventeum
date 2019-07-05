package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class NodeRecoveryTests extends BaseKafkaIntegrationTest {

    protected void doSingleNodeFailureRecoveryTest() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());

        doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 4000);
    }

    protected void doMultipleNodeFailuresRecoveryTest() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());

        doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 4000);
        doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 6000);
        doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 8000);
        doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 10000);
        doParityRestartEventEmissionsAssertion(emitter, registeredFilter, 20000);
    }

    protected void doQuickSuccessionNodeFailuresRecoveryTest() throws Exception {
        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());

        final int numRestarts = 20;

        for (int i = 0; i < numRestarts; i++) {
            restartParity(300);
        }

        emitEventAndVerify(emitter, registeredFilter);

    }

    private void doParityRestartEventEmissionsAssertion(
            EventEmitter emitter, ContractEventFilter registeredFilter, long timeToRecovery) throws Exception {
        getBroadcastContractEvents().clear();

        restartParity(timeToRecovery);

        emitEventAndVerify(emitter, registeredFilter);
    }

    private void emitEventAndVerify(EventEmitter emitter, ContractEventFilter registeredFilter) throws Exception {
        final String valueOne = UUID.randomUUID().toString().substring(0, 10);
        final String valueFour = UUID.randomUUID().toString().substring(0, 10);
        emitter.emitEvent(stringToBytes(valueOne), BigInteger.valueOf(123), valueFour).send();

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails secondEventDetails = getBroadcastContractEvents().get(0);

        verifyDummyEventDetails(registeredFilter, secondEventDetails, ContractEventStatus.UNCONFIRMED,
                toHexWithTrailingZeros(valueOne.getBytes(), 66), Keys.toChecksumAddress(CREDS.getAddress()), BigInteger.valueOf(123), valueFour);
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
