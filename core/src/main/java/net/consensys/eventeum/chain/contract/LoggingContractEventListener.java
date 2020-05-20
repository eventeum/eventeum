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

package net.consensys.eventeum.chain.contract;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that logs the contract event details.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class LoggingContractEventListener implements ContractEventListener{

    private static final Logger logger = LoggerFactory.getLogger(LoggingContractEventListener.class);

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        logger.info("Contract event fired: " + eventDetails.getName());
    }
}
