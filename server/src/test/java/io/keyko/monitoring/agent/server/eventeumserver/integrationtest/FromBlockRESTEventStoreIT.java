package io.keyko.monitoring.agent.server.eventeumserver.integrationtest;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
public class FromBlockRESTEventStoreIT extends BaseFromBlockIntegrationTest {

    @Test
    public void testFromBlockCorrectForRegisteredFilter() {
        final ContractEventFilter filter = createDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        registerEventFilter(filter);

        assertEquals(BigInteger.TEN, getFromBlockNumberForLatestRegisteredFilter());
    }
}
