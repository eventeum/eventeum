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

package net.consensys.eventeumserver.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseStartBlockTest extends BaseKafkaIntegrationTest {

  @BeforeAll
  public static void doTriggerBlocks() throws IOException {
    // Triggers 6 blocks total (genesis and additional 5)
    triggerBlocks(5);
  }

  @BeforeEach
  @Override
  public void clearMessages() {
    // Theres a race condition that sometimes causes the block messages to be cleared after being
    // received
    // Overriding to remove the clearing of block messages as its not required here (until there are
    // multiple tests!)
    getBroadcastContractEvents().clear();
    getBroadcastTransactionMessages().clear();
  }

  @Test
  public void testStartBlockForBlockBroadcast() throws IOException {
    triggerBlocks(1);

    waitForBlockMessages(5);

    assertEquals(BigInteger.valueOf(3), getBroadcastBlockMessages().get(0).getNumber());
    assertEquals(BigInteger.valueOf(4), getBroadcastBlockMessages().get(1).getNumber());
    assertEquals(BigInteger.valueOf(5), getBroadcastBlockMessages().get(2).getNumber());
    assertEquals(BigInteger.valueOf(6), getBroadcastBlockMessages().get(3).getNumber());
    assertEquals(BigInteger.valueOf(7), getBroadcastBlockMessages().get(4).getNumber());
  }

  @Override
  protected Map<String, Object> modifyKafkaConsumerProps(Map<String, Object> consumerProps) {
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return consumerProps;
  }

  private static void triggerBlocks(int numBlocks) throws IOException {
    final Web3j web3j = Web3j.build(new HttpService("http://localhost:8545"));

    for (int i = 0; i < numBlocks; i++) {
      web3j
          .ethSendTransaction(
              Transaction.createEtherTransaction(
                  web3j.ethAccounts().send().getAccounts().get(0),
                  web3j
                      .ethGetTransactionCount(
                          web3j.ethAccounts().send().getAccounts().get(0),
                          DefaultBlockParameterName.fromString("latest"))
                      .send()
                      .getTransactionCount(),
                  BigInteger.valueOf(2000),
                  BigInteger.valueOf(6721975),
                  CREDS.getAddress(),
                  new BigInteger("9460000000000000000")))
          .send();
    }
  }
}
