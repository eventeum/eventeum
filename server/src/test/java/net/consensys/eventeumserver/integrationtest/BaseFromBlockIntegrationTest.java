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
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.math.BigInteger;
import java.util.List;

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
        ArgumentCaptor<EthFilter> captor = ArgumentCaptor.forClass(EthFilter.class);

        Mockito.verify(web3j).ethLogFlowable(captor.capture());

        List<EthFilter> allInvocationArgs = captor.getAllValues();
        EthFilter lastArg = allInvocationArgs.get(allInvocationArgs.size() - 1);

        final DefaultBlockParameterNumber blockParameterNumber =
                (DefaultBlockParameterNumber) lastArg.getFromBlock();

        return blockParameterNumber.getBlockNumber();
    }
}
