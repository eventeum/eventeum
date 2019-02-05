package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.service.health.listener.NodeFailureListener;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class NodeHealthCheckServiceTest {

    private static final String VERSION = "1.0.0";

    private NodeHealthCheckService underTest;

    private BlockchainService mockBlockchainService;

    private NodeFailureListener mockFailureListener;

    @Before
    public void init() {
        mockBlockchainService = mock(BlockchainService.class);
        mockFailureListener = mock(NodeFailureListener.class);

        underTest = new NodeHealthCheckService(mockBlockchainService, mockFailureListener);
    }

    @Test
    public void testEverythingUpHappyPath() {
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener, never()).onNodeFailure();
        verify(mockFailureListener, never()).onNodeRecovery();
    }

    @Test
    public void testNodeDisconnected() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, never()).onNodeRecovery();
    }

    @Test
    public void testNodeStaysDown() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();

        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, never()).onNodeRecovery();
    }


    @Test
    public void testNodeComesBackUpNotSubscribed() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, times(1)).onNodeRecovery();
        verify(mockFailureListener, never()).onNodeSubscribed();
    }

    @Test
    public void testNodeComesBackUpSubscribed() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, never()).onNodeRecovery();
        verify(mockFailureListener, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, times(1)).onNodeRecovery();
        verify(mockFailureListener, times(1)).onNodeSubscribed();
    }

    @Test
    public void testNodeFromConnectedToSubscribed() {
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockFailureListener, never()).onNodeFailure();
        verify(mockFailureListener, times(1)).onNodeRecovery();
        verify(mockFailureListener, never()).onNodeSubscribed();


        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener, never()).onNodeFailure();
        verify(mockFailureListener, times(1)).onNodeRecovery();
        verify(mockFailureListener, times(1)).onNodeSubscribed();
    }

    @Test
    public void testNodeFromSubscribedToConnected() {
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener, never()).onNodeFailure();
        verify(mockFailureListener, never()).onNodeRecovery();
        verify(mockFailureListener, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockFailureListener, never()).onNodeFailure();
        verify(mockFailureListener, times(1)).onNodeRecovery();
        verify(mockFailureListener, never()).onNodeSubscribed();
    }

    @Test
    public void testNodeComesBackUpAndStaysUp() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, never()).onNodeRecovery();
        verify(mockFailureListener, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, times(1)).onNodeRecovery();
        verify(mockFailureListener, times(1)).onNodeSubscribed();

        underTest.checkHealth();

        verify(mockFailureListener, times(1)).onNodeFailure();
        verify(mockFailureListener, times(1)).onNodeRecovery();
        verify(mockFailureListener, times(1)).onNodeSubscribed();
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
}
