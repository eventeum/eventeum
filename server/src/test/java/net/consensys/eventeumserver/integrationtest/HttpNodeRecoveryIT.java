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

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test-db.properties")
public class HttpNodeRecoveryIT extends NodeRecoveryTests {

    @Test
    public void singleNodeFailureRecoveryTest() throws Exception {
        doSingleNodeFailureRecoveryTest();
    }

    @Test
    public void multipleNodeFailuresRecoveryTest() throws Exception {
        doMultipleNodeFailuresRecoveryTest();
    }

    @Test
    public void quickSuccessionNodeFailuresRecoveryTest() throws Exception {
        doQuickSuccessionNodeFailuresRecoveryTest();
    }
}
