package net.consensys.eventeumserver.integrationtest;

import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test-db.properties")
public class TransactionMonitorIT extends MainBroadcasterTests {

    @Test
    public void testMultipleTransactions() throws Exception {
        final String[] hashes = new String[3];

        for (int i = 0; i < 3; i++) {
            hashes[i] = doTestBroadcastsUnconfirmedTransactionAfterInitialMining();
            clearMessages();
            triggerBlocksAndCheckMessagesSize(3, 0);
            triggerBlocks(1);
        }

        waitForConfirmedTransaction(hashes[0], 1);

        triggerBlocksAndCheckMessagesSize(3, 1);
        triggerBlocks(1);
        waitForConfirmedTransaction(hashes[1], 2);

        triggerBlocksAndCheckMessagesSize(3, 2);
        triggerBlocks(1);
        waitForConfirmedTransaction(hashes[2], 3);
    }

    private void waitForConfirmedTransaction(String hash, int expectedNumMessages) {
        waitForTransactionMessages(expectedNumMessages);

        assertEquals(expectedNumMessages, getBroadcastTransactionMessages().size());
        TransactionDetails txDetails = getBroadcastTransactionMessages().get(expectedNumMessages - 1);
        assertEquals(hash, txDetails.getHash());
        assertEquals(TransactionStatus.CONFIRMED, txDetails.getStatus());
    }

    private void triggerBlocksAndCheckMessagesSize(int numBlocks, int expectedNumBroadcasts) throws InterruptedException, ExecutionException, IOException {
        for (int i = 0; i< numBlocks; i++) {
            triggerBlocks(1);
            assertEquals(expectedNumBroadcasts, getBroadcastTransactionMessages().size());
        }
    }
}
