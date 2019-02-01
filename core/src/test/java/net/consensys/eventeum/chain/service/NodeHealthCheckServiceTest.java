package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.service.health.listener.NodeFailureListener;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import net.consensys.eventeum.chain.service.health.listener.NodeFailureListeners;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class NodeHealthCheckServiceTest {

    private static final String VERSION = "1.0.0";

    private NodeHealthCheckService underTest;

    private BlockchainService mockBlockchainService;

    private NodeFailureListener mockFailureListener1;

    private NodeFailureListener mockFailureListener2;

    @Before
    public void init() {
        mockBlockchainService = mock(BlockchainService.class);
        mockFailureListener1 = mock(NodeFailureListener.class);
        mockFailureListener2 = mock(NodeFailureListener.class);

        underTest = new NodeHealthCheckService(mockBlockchainService,
                new NodeFailureListeners(Arrays.asList(mockFailureListener1, mockFailureListener2)));
    }

    @Test
    public void testEverythingUpHappyPath() {
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener1, never()).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
    }

    @Test
    public void testNodeDisconnected() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
    }

    @Test
    public void testNodeStaysDown() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeFailure();

        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
    }


    @Test
    public void testNodeComesBackUpNotSubscribed() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener1, never()).onNodeSubscribed();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener1, never()).onNodeSubscribed();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeSubscribed();
    }

    @Test
    public void testNodeComesBackUpSubscribed() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener1, never()).onNodeSubscribed();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener1, times(1)).onNodeSubscribed();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeSubscribed();
    }

    @Test
    public void testNodeFromConnectedToSubscribed() {
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockFailureListener1, never()).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener1, never()).onNodeSubscribed();
        verify(mockFailureListener2, never()).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener1, never()).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener1, times(1)).onNodeSubscribed();
        verify(mockFailureListener2, never()).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeSubscribed();
    }

    @Test
    public void testNodeFromSubscribedToConnected() {
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener1, never()).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener1, never()).onNodeSubscribed();
        verify(mockFailureListener2, never()).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(false);
        underTest.checkHealth();

        verify(mockFailureListener1, never()).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener1, never()).onNodeSubscribed();
        verify(mockFailureListener2, never()).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeSubscribed();
    }

    @Test
    public void testNodeComesBackUpAndStaysUp() {
        wireBlockchainServiceDown(false, false);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener1, never()).onNodeSubscribed();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeSubscribed();

        reset(mockBlockchainService);
        wireBlockchainServiceUp(true);
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener1, times(1)).onNodeSubscribed();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeSubscribed();

        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener1, times(1)).onNodeSubscribed();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeSubscribed();
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
