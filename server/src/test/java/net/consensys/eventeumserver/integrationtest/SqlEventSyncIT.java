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

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestPropertySource(locations = "classpath:application-test-sql-event-sync.properties")
@ContextConfiguration(initializers = {SqlEventSyncIT.Initializer.class})
@Testcontainers
public class SqlEventSyncIT extends BaseEventCatchupTest {

  @Container
  public static MSSQLServerContainer mssqlserver =
      new MSSQLServerContainer().withPassword("reallyStrongPwd123").acceptLicense();

  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of("spring.datasource.url=" + mssqlserver.getJdbcUrl())
          .applyTo(configurableApplicationContext.getEnvironment());
    }
  }
}
