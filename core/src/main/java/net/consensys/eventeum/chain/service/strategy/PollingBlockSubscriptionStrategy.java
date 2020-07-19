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

package net.consensys.eventeum.chain.service.strategy;

import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.block.BlockNumberService;
import net.consensys.eventeum.chain.service.domain.Block;
import net.consensys.eventeum.chain.service.domain.wrapper.Web3jBlock;
import net.consensys.eventeum.service.AsyncTaskService;
import net.consensys.eventeum.utils.JSON;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.Optional;

@Slf4j
public class PollingBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy<EthBlock> {

    public PollingBlockSubscriptionStrategy(Web3j web3j,
                                            String nodeName,
                                            AsyncTaskService asyncService,
                                            BlockNumberService blockNumberService) {
        super(web3j, nodeName, asyncService, blockNumberService);
    }

    @Override
    public Disposable subscribe() {

        final BigInteger startBlock = getStartBlock();

        log.info("Starting block polling, from block {}", startBlock);

        final DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(startBlock);

        blockSubscription = web3j
                .replayPastAndFutureBlocksFlowable(blockParam, true)
                .doOnError((error) -> onError(blockSubscription, error))
                .subscribe(block -> triggerListeners(block), (error) -> onError(blockSubscription, error));


        return blockSubscription;
    }

    @Override
    Block convertToEventeumBlock(EthBlock blockObject) {
        //Infura is sometimes returning null blocks...just ignore in this case.
        if (blockObject == null || blockObject.getBlock() == null) {
            return null;
        }

        try {
            return new Web3jBlock(blockObject.getBlock(), nodeName);
        } catch (Throwable t) {
            log.error("Error converting block: " + JSON.stringify(blockObject), t);
            throw t;
        }
    }
}
