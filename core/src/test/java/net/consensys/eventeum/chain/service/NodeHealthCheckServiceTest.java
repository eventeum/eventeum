package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.service.health.strategy.ReconnectionStrategy;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;

public class NodeHealthCheckServiceTest {

    private static final String VERSION = "1.0.0";

    private NodeHealthCheckService underTest;

    private BlockchainService mockBlockchainService;

    private ReconnectionStrategy mockReconnectionStrategy;

    private AtomicBoolean isConnnected = new AtomicBoolean(false);

    @Before
    public void init() {
        mockBlockchainService = mock(BlockchainService.class);
        mockReconnectionStrategy = mock(ReconnectionStrategy.class);

        underTest = new NodeHealthCheckService(mockBlockchainService, mockReconnectionStrategy);
    }

    @Test
    public void testEverythingUpHappyPath() {
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, never()).reconnect();
        verify(mockReconnectionStrategy, never()).resubscribe();
    }

    @Test
    public void testNodeDisconnectedReconnectSuccess() {
        wireBlockchainServiceDown(false, false);
        wireReconnectResult(true);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();
    }

    @Test
    public void testNodeDisconnectedReconnectFailure() {
        wireBlockchainServiceDown(false, false);
        wireReconnectResult(false);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, never()).resubscribe();
    }

    @Test
    public void testNodeStaysDown() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();

        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(2)).reconnect();
        verify(mockReconnectionStrategy, never()).resubscribe();
    }


    @Test
    public void testNodeComesBackUpNotSubscribed() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, never()).resubscribe();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();
    }

    @Test
    public void testNodeFromConnectedToSubscribed() {
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, never()).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, never()).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();
    }

    @Test
    public void testNodeFromSubscribedToConnected() {
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, never()).reconnect();
        verify(mockReconnectionStrategy, never()).resubscribe();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, never()).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();
    }

    @Test
    public void testNodeComesBackUpAndStaysUp() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, never()).resubscribe();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();

        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();
    }

    private void wireBlockchainServiceUp(boolean isSubscribed) {
        when(mockBlockchainService.getClientVersion()).thenReturn(VERSION);
        when(mockBlockchainService.isConnected()).thenReturn(isSubscribed);
    }

    private void wireBlockchainServiceDown(boolean isConnected, boolean isSubscribed) {

        when(mockBlockchainService.isConnected()).thenReturn(isSubscribed);
        if (isConnected) {
            when(mockBlockchainService.getClientVersion()).thenReturn(VERSION);
        } else {
            when(mockBlockchainService.getClientVersion()).thenThrow(
                    new BlockchainException("Error!", new IOException("")));
        }
    }

    private void wireReconnectResult(boolean reconnectSuccess) {
        isConnnected.set(false);

        doAnswer((invocation) -> {
            if (reconnectSuccess) {
                isConnnected.set(true);
            } else {
                isConnnected.set(false);
            }
            return null;
        }).when(mockReconnectionStrategy).reconnect();

        doAnswer((invocation) -> {
            if (isConnnected.get()) {
                return VERSION;
            } else {
                throw new BlockchainException("Error!", new IOException(""));
            }
        }).when(mockBlockchainService).getClientVersion();
    }
}
