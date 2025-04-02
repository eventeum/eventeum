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

package net.consensys.eventeum.chain.service.block;

import com.google.common.collect.EvictingQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import net.consensys.eventeum.chain.service.domain.Block;
import org.springframework.stereotype.Component;

@Component
public class DefaultBlockCache implements BlockCache {

  private static final Integer CACHE_SIZE = 3;

  private EvictingQueue<Block> cachedBlocks = EvictingQueue.create(CACHE_SIZE);

  private ReentrantLock lock = new ReentrantLock();

  @Override
  public void add(Block block) {
    lock.lock();

    try {
      cachedBlocks.add(block);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Set<Block> getCachedBlocks() {
    lock.lock();

    try {
      final Set<Block> cachedBlocksSet = new HashSet<>();
      cachedBlocksSet.addAll(cachedBlocks);

      return cachedBlocksSet;
    } finally {
      lock.unlock();
    }
  }
}
