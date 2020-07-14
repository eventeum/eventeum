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

package net.consensys.eventeum.chain.config.factory;

import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.block.EventBlockManagementService;
import net.consensys.eventeum.chain.service.Web3jService;
import net.consensys.eventeum.chain.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.service.strategy.BlockSubscriptionStrategy;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.service.AsyncTaskService;
import org.springframework.beans.factory.FactoryBean;
import org.web3j.protocol.Web3j;

@Data
public class BlockchainServiceFactoryBean implements FactoryBean<BlockchainService> {

    private Node node;
    private Web3j web3j;
    private ContractEventDetailsFactory contractEventDetailsFactory;
    private AsyncTaskService asyncTaskService;

    @Override
    public BlockchainService getObject() throws Exception {
        return new Web3jService(node.getName(), web3j, contractEventDetailsFactory, asyncTaskService);
    }

    @Override
    public Class<?> getObjectType() {
        return BlockchainService.class;
    }
}
