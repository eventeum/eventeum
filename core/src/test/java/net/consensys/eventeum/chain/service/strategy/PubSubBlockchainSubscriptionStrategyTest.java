package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.dto.block.BlockDetails;
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

    //12345678
    private static final String BLOCK_TIMESTAMP = "0xbc614e";

    private static final String NODE_NAME = "mainnet";

    private PubSubBlockSubscriptionStrategy underTest;

    private PublishSubject<NewHeadsNotification> blockSubject;

    private Web3j mockWeb3j;

    private NewHeadsNotification mockNewHeadsNotification;

    private NewHead mockNewHead;

    private BlockListener mockBlockListener;

    @Before
    public void init() throws IOException {
        this.mockWeb3j = mock(Web3j.class);

        mockNewHeadsNotification = mock(NewHeadsNotification.class);
        when(mockNewHeadsNotification.getParams()).thenReturn(new NewHeadNotificationParameter());

        mockNewHead = mock(NewHead.class);
        when(mockNewHead.getNumber()).thenReturn(BLOCK_NUMBER_HEX);
        when(mockNewHead.getHash()).thenReturn(BLOCK_HASH);
        when(mockNewHead.getTimestamp()).thenReturn(BLOCK_TIMESTAMP);

        blockSubject = PublishSubject.create();
        when(mockWeb3j.newHeadsNotifications()).thenReturn(blockSubject);

        underTest = new PubSubBlockSubscriptionStrategy(mockWeb3j, NODE_NAME);
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

    @Test
    public void testBlockTimestampPassedToListenerIsCorrect() {
        underTest.subscribe();
        final BlockDetails blockDetails = doRegisterBlockListenerAndTrigger();

        assertEquals(BigInteger.valueOf(12345678), blockDetails.getTimestamp());
    }

    @Test
    public void testBlockNodeNamePassedToListenerIsCorrect() {
        underTest.subscribe();
        final BlockDetails blockDetails = doRegisterBlockListenerAndTrigger();

        assertEquals(NODE_NAME, blockDetails.getNodeName());
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
