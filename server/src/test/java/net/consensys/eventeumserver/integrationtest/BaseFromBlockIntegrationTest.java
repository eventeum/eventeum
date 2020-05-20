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

import net.consensys.eventeum.chain.service.Web3jService;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;

public class BaseFromBlockIntegrationTest extends BaseKafkaIntegrationTest {

    @Autowired
    private ChainServicesContainer chainServicesContainer;

    private Web3j web3j;

    @Before
    public void spyOnWeb3j() {
        final NodeServices nodeServices = chainServicesContainer.getNodeServices(
                Constants.DEFAULT_NODE_NAME);

        final Web3jService web3jService = (Web3jService) nodeServices.getBlockchainService();

        web3j = Mockito.spy(web3jService.getWeb3j());
        web3jService.setWeb3j(web3j);
    }


    protected BigInteger getFromBlockNumberForLatestRegisteredFilter() {
        ArgumentCaptor<DefaultBlockParameter> captor = ArgumentCaptor.forClass(DefaultBlockParameter.class);

        Mockito.verify(web3j).replayPastAndFutureBlocksFlowable(captor.capture(), eq(true));

        List<DefaultBlockParameter> allInvocationArgs = captor.getAllValues();
        DefaultBlockParameterNumber lastArg = (DefaultBlockParameterNumber)
                allInvocationArgs.get(allInvocationArgs.size() - 1);

        return lastArg.getBlockNumber();
    }
}
