package net.consensys.eventeum.chain.service.strategy;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.service.EventStoreService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.protocol.websocket.events.NewHeadsNotification;
import org.web3j.protocol.websocket.events.NotificationParams;

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

    private PublishProcessor<NewHeadsNotification> blockPublishProcessor;

    private Web3j mockWeb3j;

    private NewHeadsNotification mockNewHeadsNotification;

    private NewHead mockNewHead;

    private BlockListener mockBlockListener;

    private EventStoreService mockEventStoreService;

    @Before
    public void init() throws IOException {
        this.mockWeb3j = mock(Web3j.class);

        mockNewHeadsNotification = mock(NewHeadsNotification.class);
        mockEventStoreService = mock(EventStoreService.class);
        when(mockNewHeadsNotification.getParams()).thenReturn(new NewHeadNotificationParameter());

        mockNewHead = mock(NewHead.class);
        when(mockNewHead.getNumber()).thenReturn(BLOCK_NUMBER_HEX);
        when(mockNewHead.getHash()).thenReturn(BLOCK_HASH);
        when(mockNewHead.getTimestamp()).thenReturn(BLOCK_TIMESTAMP);

        blockPublishProcessor = PublishProcessor.create();
        when(mockWeb3j.newHeadsNotifications()).thenReturn(blockPublishProcessor);

        underTest = new PubSubBlockSubscriptionStrategy(mockWeb3j, NODE_NAME, mockEventStoreService);
    }

    @Test
    public void testSubscribe() {
        final Disposable returnedSubscription = underTest.subscribe();

        assertEquals(false, returnedSubscription.isDisposed());
    }

    @Test
    public void testUnsubscribe() {
        final Disposable returnedSubscription = underTest.subscribe();

        assertEquals(false, returnedSubscription.isDisposed());

        underTest.unsubscribe();

        assertEquals(true, returnedSubscription.isDisposed());
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

        blockPublishProcessor.onNext(mockNewHeadsNotification);

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

        blockPublishProcessor.onNext(mockNewHeadsNotification);

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
