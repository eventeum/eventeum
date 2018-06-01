package net.consensys.eventeum.integrationtest;

import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test-db.properties")
public class FromBlockDBEventStoreIT extends BaseFromBlockIntegrationTest {

    @Autowired
    private ContractEventDetailsRepository repo;

    @Test
    public void testFromBlockCorrectForRegisteredFilter() {
        final ContractEventFilter filter = createDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        final ContractEventDetails eventDetails = new ContractEventDetails();
        eventDetails.setBlockNumber(BigInteger.valueOf(123));
        eventDetails.setEventSpecificationSignature(Web3jUtil.getSignature(filter.getEventSpecification()));

        repo.save(eventDetails);

        registerEventFilter(filter);

        assertEquals(BigInteger.valueOf(123), getFromBlockNumberForLatestRegisteredFilter());
    }
}
