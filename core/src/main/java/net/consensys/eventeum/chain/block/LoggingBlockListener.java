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

package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.service.domain.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A block listener that logs the block details.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class LoggingBlockListener implements BlockListener {

  private static final Logger logger = LoggerFactory.getLogger(LoggingBlockListener.class);

  @Override
  public void onBlock(Block block) {
    logger.info("New block mined. Hash: {}, Number: {}", block.getHash(), block.getNumber());
  }
}
