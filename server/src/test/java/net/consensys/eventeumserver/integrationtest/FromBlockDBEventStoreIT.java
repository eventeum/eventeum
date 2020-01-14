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

import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.integration.eventstore.db.repository.ContractEventDetailsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-db.properties")
public class FromBlockDBEventStoreIT extends BaseFromBlockIntegrationTest {

    @Autowired
    private ContractEventDetailsRepository repo;

    @Test
    public void testFromBlockCorrectForRegisteredFilter() {
        final ContractEventFilter filter = createDummyEventFilter(FAKE_CONTRACT_ADDRESS);

        repo.save(createContractEventDetails(filter.getEventSpecification(), BigInteger.valueOf(15)));
        repo.save(createContractEventDetails(filter.getEventSpecification(), BigInteger.valueOf(123)));
        repo.save(createContractEventDetails(filter.getEventSpecification(), BigInteger.valueOf(1002)));
        repo.save(createContractEventDetails(filter.getEventSpecification(), BigInteger.valueOf(2004)));
        repo.save(createContractEventDetails(filter.getEventSpecification(), BigInteger.valueOf(209)));
        repo.save(createContractEventDetails(filter.getEventSpecification(),
                BigInteger.valueOf(2005), "0xa8c985f41f10a084d353e47f2529f35c4ba13ca9"));

        registerEventFilter(filter);

        assertEquals(BigInteger.valueOf(2004), getFromBlockNumberForLatestRegisteredFilter());
    }

    private ContractEventDetails createContractEventDetails(ContractEventSpecification eventSpec, BigInteger blockNumber) {

        return createContractEventDetails(eventSpec, blockNumber, FAKE_CONTRACT_ADDRESS);
    }

    private ContractEventDetails createContractEventDetails(
            ContractEventSpecification eventSpec, BigInteger blockNumber, String contractAddress) {

        final ContractEventDetails eventDetails = new ContractEventDetails();
        eventDetails.setBlockNumber(blockNumber);
        eventDetails.setEventSpecificationSignature(Web3jUtil.getSignature(eventSpec));
        eventDetails.setAddress(contractAddress);

        return eventDetails;
    }
}
