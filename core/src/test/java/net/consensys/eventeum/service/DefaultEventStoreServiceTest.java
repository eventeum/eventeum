package net.consensys.eventeum.service;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventStoreServiceTest {

    private static final String EVENT_SIGNATURE = "signture";

    private DefaultEventStoreService underTest;

    @Mock
    private EventStore mockEventStore;

    @Mock
    private ContractEventDetails mockEventDetails1;

    @Mock
    private ContractEventDetails mockEventDetails2;

    @Mock
    private Page<ContractEventDetails> mockPage;

    @Before
    public void init() {
        underTest = new DefaultEventStoreService(mockEventStore);
    }

    @Test
    public void testGetLatestContractEvent() {
        when(mockPage.getContent()).thenReturn(Arrays.asList(mockEventDetails1, mockEventDetails2));
        when(mockEventStore.getContractEventsForSignature(
                eq(EVENT_SIGNATURE), any(PageRequest.class))).thenReturn(mockPage);
        assertEquals(mockEventDetails1, underTest.getLatestContractEvent(EVENT_SIGNATURE).get());
    }

    @Test
    public void testGetLatestContractEventNullEvents() {
        when(mockPage.getContent()).thenReturn(null);
        when(mockEventStore.getContractEventsForSignature(
                eq(EVENT_SIGNATURE), any(PageRequest.class))).thenReturn(mockPage);
        assertEquals(false, underTest.getLatestContractEvent(EVENT_SIGNATURE).isPresent());
    }

    @Test
    public void testGetLatestContractEventEmptyEvents() {
        when(mockPage.getContent()).thenReturn(new ArrayList<>());
        when(mockEventStore.getContractEventsForSignature(
                eq(EVENT_SIGNATURE), any(PageRequest.class))).thenReturn(mockPage);
        assertEquals(false, underTest.getLatestContractEvent(EVENT_SIGNATURE).isPresent());
    }
}
