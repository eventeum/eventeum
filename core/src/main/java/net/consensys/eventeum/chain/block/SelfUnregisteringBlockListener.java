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

import net.consensys.eventeum.chain.service.BlockchainService;

/**
 * An abstract implementation of a block listener that can unregister itself from the system.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public abstract class SelfUnregisteringBlockListener implements BlockListener {

    private BlockchainService blockchainService;

    protected SelfUnregisteringBlockListener(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    protected void unregister() {
        blockchainService.removeBlockListener(this);
    }
}
