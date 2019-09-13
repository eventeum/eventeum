package net.consensys.eventeum.chain.service.strategy;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import net.consensys.eventeum.chain.block.BlockListener;
import net.consensys.eventeum.chain.service.Web3jService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.service.EventStoreService;
import net.consensys.eventeum.testutils.DummyAsyncTaskService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;

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

    private PublishProcessor<EthBlock> blockPublishProcessor;

    private Web3j mockWeb3j;

    private EthBlock mockEthBlock;

    private BlockListener mockBlockListener;

    private EventStoreService mockEventStoreService;

    @Before
    public void init() {
        this.mockWeb3j = mock(Web3j.class);

        mockEthBlock = mock(EthBlock.class);
        mockEventStoreService = mock(EventStoreService.class);
        final EthBlock.Block mockBlock = mock(EthBlock.Block.class);

        when(mockBlock.getNumber()).thenReturn(BLOCK_NUMBER);
        when(mockBlock.getHash()).thenReturn(BLOCK_HASH);
        when(mockBlock.getTimestamp()).thenReturn(BLOCK_TIMESTAMP);
        when(mockEthBlock.getBlock()).thenReturn(mockBlock);

        blockPublishProcessor = PublishProcessor.create();
        when(mockWeb3j.blockFlowable(true)).thenReturn(blockPublishProcessor);

        underTest = new PollingBlockSubscriptionStrategy(mockWeb3j, NODE_NAME, mockEventStoreService);
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
        final Block block = doRegisterBlockListenerAndTrigger();
        assertNotNull(block);
    }

    @Test
    public void testRemoveBlockListener() {
        underTest.subscribe();
        final Block block = doRegisterBlockListenerAndTrigger();
        assertNotNull(block);

        reset(mockBlockListener);
        underTest.removeBlockListener(mockBlockListener);

        blockPublishProcessor.onNext(mockEthBlock);

        verify(mockBlockListener, never()).onBlock(any());
    }

    @Test
    public void testBlockHashPassedToListenerIsCorrect() {
        underTest.subscribe();
        final Block block = doRegisterBlockListenerAndTrigger();

        assertEquals(BLOCK_HASH, block.getHash());
    }

    @Test
    public void testBlockNumberPassedToListenerIsCorrect() {
        underTest.subscribe();
        final Block block = doRegisterBlockListenerAndTrigger();

        assertEquals(BLOCK_NUMBER, block.getNumber());
    }

    @Test
    public void testBlockTimestampPassedToListenerIsCorrect() {
        underTest.subscribe();
        final Block block = doRegisterBlockListenerAndTrigger();

        assertEquals(BLOCK_TIMESTAMP, block.getTimestamp());
    }

    @Test
    public void testBlockNodeNamePassedToListenerIsCorrect() {
        underTest.subscribe();
        final Block block = doRegisterBlockListenerAndTrigger();

        assertEquals(NODE_NAME, block.getNodeName());
    }

    private Block doRegisterBlockListenerAndTrigger() {

        mockBlockListener = mock(BlockListener.class);
        underTest.addBlockListener(mockBlockListener);

        blockPublishProcessor.onNext(mockEthBlock);

        final ArgumentCaptor<Block> captor = ArgumentCaptor.forClass(Block.class);
        verify(mockBlockListener).onBlock(captor.capture());

        return captor.getValue();
    }
}
