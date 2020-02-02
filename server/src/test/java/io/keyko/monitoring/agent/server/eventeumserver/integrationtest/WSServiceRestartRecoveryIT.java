package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations="classpath:application-test-ws-pubsub.properties")
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
