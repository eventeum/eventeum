/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.chain.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.consensys.eventeum.chain.service.health.NodeHealthCheckService;
import net.consensys.eventeum.chain.service.health.strategy.ReconnectionStrategy;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.integration.eventstore.SaveableEventStore;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.monitoring.EventeumValueMonitor;
import net.consensys.eventeum.monitoring.MicrometerValueMonitor;
import net.consensys.eventeum.service.EventStoreService;
import net.consensys.eventeum.service.SubscriptionService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;

public class NodeHealthCheckServiceTest {

    private static final BigInteger BLOCK_NUMBER = BigInteger.valueOf(1234);

    private static final Integer SYNCING_THRESHOLD = Integer.valueOf(60);

    private static final Long HEALTH_CHECK_INTERVAL = 1000l;

    private NodeHealthCheckService underTest;

    private BlockchainService mockBlockchainService;

    private BlockSubscriptionStrategy mockBlockSubscriptionStrategy;

    private ReconnectionStrategy mockReconnectionStrategy;

    private SubscriptionService mockSubscriptionService;

    private EventeumValueMonitor mockEventeumValueMonitor;

    private EventStoreService mockEventStoreService;

    private ScheduledThreadPoolExecutor  mockTaskScheduler;

    @Before
    public void init() throws Exception {
        mockBlockchainService = mock(BlockchainService.class);
        when(mockBlockchainService.getNodeName()).thenReturn(Constants.DEFAULT_NODE_NAME);
        mockBlockSubscriptionStrategy = mock(BlockSubscriptionStrategy.class);
        when(mockBlockSubscriptionStrategy.getNodeName()).thenReturn(Constants.DEFAULT_NODE_NAME);

        mockReconnectionStrategy = mock(ReconnectionStrategy.class);
        mockSubscriptionService = mock(SubscriptionService.class);
        when(mockSubscriptionService.getState()).thenReturn(SubscriptionService.SubscriptionServiceState.SUBSCRIBED);

        mockEventStoreService = mock(EventStoreService.class);
        LatestBlock latestBlock = new LatestBlock();
        latestBlock.setNumber(BLOCK_NUMBER);
        when(mockEventStoreService.getLatestBlock(any())).thenReturn(Optional.of(latestBlock));
        mockEventeumValueMonitor = new MicrometerValueMonitor(new SimpleMeterRegistry());
        mockTaskScheduler = mock(ScheduledThreadPoolExecutor.class);

        underTest = createUnderTest();

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

        verify(mockReconnectionStrategy, times(2)).reconnect();
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

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();
    }

    @Test
    public void testNodeComesBackUpAndStaysUp() {
        wireBlockchainServiceDown(true, false);

        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();

        reset(mockBlockchainService);
        reset(mockSubscriptionService);

        wireBlockchainServiceUp(true);

        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();

        underTest.checkHealth();

        verify(mockReconnectionStrategy, times(1)).reconnect();
        verify(mockReconnectionStrategy, times(1)).resubscribe();
    }

    private void wireBlockchainServiceUp(boolean isSubscribed) {
        when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(BLOCK_NUMBER);
        when(mockBlockSubscriptionStrategy.isSubscribed()).thenReturn(isSubscribed);
        when(mockBlockchainService.getNodeName()).thenReturn(Constants.DEFAULT_NODE_NAME);
    }

    private void wireBlockchainServiceDown(boolean isConnected, boolean isSubscribed) {

        when(mockBlockSubscriptionStrategy.isSubscribed()).thenReturn(isSubscribed);
        if (isConnected) {
            when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(BLOCK_NUMBER);
        } else {
            when(mockBlockchainService.getCurrentBlockNumber()).thenThrow(
                    new BlockchainException("Error!", new IOException("")));
        }
    }

    private AtomicBoolean isConnected = new AtomicBoolean(false);

    private void wireReconnectResult(boolean reconnectSuccess) {
        isConnected.set(false);

        doAnswer((invocation) -> {
            if (reconnectSuccess) {
                isConnected.set(true);
            } else {
                isConnected.set(false);
            }
            return null;
        }).when(mockReconnectionStrategy).reconnect();

        doAnswer((invocation) -> {
            if (isConnected.get()) {
                return BLOCK_NUMBER;
            } else {
                throw new BlockchainException("Error!", new IOException(""));
            }
        }).when(mockBlockchainService).getCurrentBlockNumber();
    }

    private NodeHealthCheckService createUnderTest() throws Exception {
        final NodeHealthCheckService healthCheckService =
                new NodeHealthCheckService(
                        mockBlockchainService,
                        mockBlockSubscriptionStrategy,
                        mockReconnectionStrategy,
                        mockSubscriptionService,
                        mockEventeumValueMonitor,
                        mockEventStoreService,
                        SYNCING_THRESHOLD,
                        mockTaskScheduler,
                        HEALTH_CHECK_INTERVAL
                );

        return healthCheckService;
    }
}
