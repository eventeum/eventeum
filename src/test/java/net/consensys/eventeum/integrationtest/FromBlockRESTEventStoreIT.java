package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
public class FromBlockRESTEventStoreIT extends BaseFromBlockIntegrationTest {

    @Test
    public void testFromBlockCorrectForRegisteredFilter() {
        final ContractEventFilter filter = createDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        registerEventFilter(filter);

        assertEquals(BigInteger.TEN, getFromBlockNumberForLatestRegisteredFilter());
    }
}
