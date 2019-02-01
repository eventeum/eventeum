package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.Web3jService;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscription;
import rx.subjects.PublishSubject;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class PollingBlockchainSubscriptionStrategyTest {

    private static final String BLOCK_HASH = "0xc0e07697167c58f2a173df45f5c9b2c46ca0941cdf0bf79616d53dc92f62aebd";

    private static final BigInteger BLOCK_NUMBER = BigInteger.valueOf(123);

    private static final BigInteger BLOCK_TIMESTAMP = BigInteger.valueOf(12345678);

    private static final String NODE_NAME = "mainnet";

    private PollingBlockSubscriptionStrategy underTest;

    private PublishSubject<EthBlock> blockSubject;

    private Web3j mockWeb3j;

    private EthBlock mockEthBlock;

    private BlockListener mockBlockListener;

    @Before
    public void init() {
        this.mockWeb3j = mock(Web3j.class);

        mockEthBlock = mock(EthBlock.class);
        final EthBlock.Block mockBlock = mock(EthBlock.Block.class);

        when(mockBlock.getNumber()).thenReturn(BLOCK_NUMBER);
        when(mockBlock.getHash()).thenReturn(BLOCK_HASH);
        when(mockBlock.getTimestamp()).thenReturn(BLOCK_TIMESTAMP);
        when(mockEthBlock.getBlock()).thenReturn(mockBlock);

        blockSubject = PublishSubject.create();
        when(mockWeb3j.blockObservable(false)).thenReturn(blockSubject);

        underTest = new PollingBlockSubscriptionStrategy(mockWeb3j, NODE_NAME);
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

        blockSubject.onNext(mockEthBlock);

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

        assertEquals(BLOCK_TIMESTAMP, blockDetails.getTimestamp());
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

        blockSubject.onNext(mockEthBlock);

        final ArgumentCaptor<BlockDetails> captor = ArgumentCaptor.forClass(BlockDetails.class);
        verify(mockBlockListener).onBlock(captor.capture());

        return captor.getValue();
    }
}
