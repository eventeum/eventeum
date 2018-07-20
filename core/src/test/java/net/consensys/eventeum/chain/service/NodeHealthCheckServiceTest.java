package net.consensys.eventeum.chain.service;

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
                Arrays.asList(mockFailureListener1, mockFailureListener2));
    }

    @Test
    public void testEverythingUpHappyPath() {
        wireBlockchainServiceUp();
        underTest.checkHealth();

        verify(mockFailureListener1, never()).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener2, never()).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
    }

    @Test
    public void testBlockchainServiceGoesDown() {
        wireBlockchainServiceDown();
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();
    }

    @Test
    public void testBlockchainServiceStaysDown() {
        wireBlockchainServiceDown();
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
    public void testBlockchainServiceComesBackUp() {
        wireBlockchainServiceDown();
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();

        reset(mockBlockchainService);
        wireBlockchainServiceUp();
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
    }

    @Test
    public void testBlockchainServiceComesBackUpAndStaysUp() {
        wireBlockchainServiceDown();
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, never()).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, never()).onNodeRecovery();

        reset(mockBlockchainService);
        wireBlockchainServiceUp();
        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();

        underTest.checkHealth();

        verify(mockFailureListener1, times(1)).onNodeFailure();
        verify(mockFailureListener1, times(1)).onNodeRecovery();
        verify(mockFailureListener2, times(1)).onNodeFailure();
        verify(mockFailureListener2, times(1)).onNodeRecovery();
    }

    private void wireBlockchainServiceUp() {
        when(mockBlockchainService.getClientVersion()).thenReturn(VERSION);
    }

    private void wireBlockchainServiceDown() {
        when(mockBlockchainService.getClientVersion()).thenThrow(
                new BlockchainException("Error!", new IOException("")));
    }
}
