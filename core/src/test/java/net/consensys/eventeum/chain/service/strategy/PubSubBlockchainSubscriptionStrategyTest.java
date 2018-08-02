package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.protocol.websocket.events.NewHeadsNotification;
import org.web3j.protocol.websocket.events.NotificationParams;
import rx.Subscription;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class PubSubBlockchainSubscriptionStrategyTest {

    private static final String BLOCK_HASH = "0xc0e07697167c58f2a173df45f5c9b2c46ca0941cdf0bf79616d53dc92f62aebd";

    private static final BigInteger BLOCK_NUMBER = BigInteger.valueOf(123);

    private static final String BLOCK_NUMBER_HEX = "0x7B";

    private PubSubBlockSubscriptionStrategy underTest;

    private PublishSubject<NewHeadsNotification> blockSubject;

    private Web3j mockWeb3j;

    private NewHeadsNotification mockNewHeadsNotification;

    private NewHead mockNewHead;

    private EthBlock mockEthBlock;

    private BlockListener mockBlockListener;

    @Before
    public void init() throws IOException {
        this.mockWeb3j = mock(Web3j.class);

        mockEthBlock = mock(EthBlock.class);
        final EthBlock.Block mockBlock = mock(EthBlock.Block.class);

        when(mockBlock.getNumber()).thenReturn(BLOCK_NUMBER);
        when(mockBlock.getHash()).thenReturn(BLOCK_HASH);
        when(mockEthBlock.getBlock()).thenReturn(mockBlock);

        mockNewHeadsNotification = mock(NewHeadsNotification.class);
        when(mockNewHeadsNotification.getParams()).thenReturn(new NewHeadNotificationParameter());

        mockNewHead = mock(NewHead.class);
        when(mockNewHead.getNumber()).thenReturn(BLOCK_NUMBER_HEX);

        blockSubject = PublishSubject.create();
        when(mockWeb3j.newHeadsNotifications()).thenReturn(blockSubject);

        final Request<?, EthBlock> mockRequest = mock(Request.class);
        when(mockRequest.send()).thenReturn(mockEthBlock);

        doReturn(mockRequest).when(mockWeb3j).ethGetBlockByNumber(any(), eq(false));

        underTest = new PubSubBlockSubscriptionStrategy(mockWeb3j, new DummyAsyncTaskService());
    }

    @Test
    public void testSubscribe() {
        final Subscription returnedSubscription = underTest.subscribe();

        assertEquals(false, returnedSubscription.isUnsubscribed());
    }

    @Test
    public void testUnsubscribe() {
        final Subscription returnedSubscription = underTest.subscribe();

        assertEquals(false, returnedSubscription.isUnsubscribed());

        underTest.unsubscribe();

        assertEquals(true, returnedSubscription.isUnsubscribed());
    }

    @Test
    public void testAddBlockListener() {
        underTest.subscribe();
        final BlockDetails blockDetails = doRegisterBlockListenerAndTrigger();
        assertNotNull(blockDetails);
    }

    @Test
    public void testRemoveBlockListener() {
        underTest.subscribe();
        final BlockDetails blockDetails = doRegisterBlockListenerAndTrigger();
        assertNotNull(blockDetails);

        reset(mockBlockListener);
        underTest.removeBlockListener(mockBlockListener);

        blockSubject.onNext(mockNewHeadsNotification);

        verify(mockBlockListener, never()).onBlock(any());
    }

    @Test
    public void testBlockHashPassedToListenerIsCorrect() {
        underTest.subscribe();
        final BlockDetails blockDetails = doRegisterBlockListenerAndTrigger();

        assertEquals(BLOCK_HASH, blockDetails.getHash());
    }

    @Test
    public void testBlockNumberPassedToListenerIsCorrect() {
        underTest.subscribe();
        final BlockDetails blockDetails = doRegisterBlockListenerAndTrigger();

        assertEquals(BLOCK_NUMBER, blockDetails.getNumber());
    }

    private BlockDetails doRegisterBlockListenerAndTrigger() {

        mockBlockListener = mock(BlockListener.class);
        underTest.addBlockListener(mockBlockListener);

        blockSubject.onNext(mockNewHeadsNotification);

        final ArgumentCaptor<BlockDetails> captor = ArgumentCaptor.forClass(BlockDetails.class);
        verify(mockBlockListener).onBlock(captor.capture());

        return captor.getValue();
    }

    private class NewHeadNotificationParameter extends NotificationParams<NewHead> {
        @Override
        public NewHead getResult() {
            return mockNewHead;
        }
    }
}
