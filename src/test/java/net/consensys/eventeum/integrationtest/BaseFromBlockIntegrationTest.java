package net.consensys.eventeum.integrationtest;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.math.BigInteger;
import java.util.List;

public class BaseFromBlockIntegrationTest extends BaseIntegrationTest {

    @SpyBean
    private Web3j web3j;

    protected BigInteger getFromBlockNumberForLatestRegisteredFilter() {
        ArgumentCaptor<EthFilter> captor = ArgumentCaptor.forClass(EthFilter.class);

        Mockito.verify(web3j).ethLogObservable(captor.capture());

        List<EthFilter> allInvocationArgs = captor.getAllValues();
        EthFilter lastArg = allInvocationArgs.get(allInvocationArgs.size() - 1);

        final DefaultBlockParameterNumber blockParameterNumber =
                (DefaultBlockParameterNumber) lastArg.getFromBlock();

        return blockParameterNumber.getBlockNumber();
    }
}
