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
@TestPropertySource(locations="classpath:application-test-ws-pubsub.properties")
public class WebsocketsNodeRecoveryIT extends NodeRecoveryTests {

    @Test
    public void singleNodeFailureRecoveryTest() throws Exception {
        doSingleNodeFailureRecoveryTest();
    }

    @Test
    public void multipleNodeFailuresRecoveryTest() throws Exception {
        doMultipleNodeFailuresRecoveryTest();
    }

    @Test
    public void quickSuccessionNodeFailuresRecoveryTest() throws Exception { doQuickSuccessionNodeFailuresRecoveryTest();
    }
}
