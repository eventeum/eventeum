package net.consensys.eventeumserver.integrationtest;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations="classpath:application-test-ws-pubsub.properties")
public class WSServiceRestartRecoveryIT extends ServiceRestartRecoveryTests {

    @Test
    public void broadcastMissedBlocksOnStartupAfterFailureTest() throws Exception {
        doBroadcastMissedBlocksOnStartupAfterFailureTest();
    }
}
