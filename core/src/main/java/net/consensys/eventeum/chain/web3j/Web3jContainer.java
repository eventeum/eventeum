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

package net.consensys.eventeum.chain.web3j;

import lombok.Data;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;

@Data
public class Web3jContainer {

    private Web3jFactory factory;

    private Web3j web3j;

    private Web3jService web3jService;

    public Web3jContainer(Web3jFactory factory) {
        setWeb3jValues(factory.build());
    }

    public synchronized Web3j getWeb3j() {
        return web3j;
    }

    public synchronized void reinitialise() {
        setWeb3jValues(factory.build());
    }

    private void setWeb3jValues(Web3jFactory.Web3jAndService web3jAndService) {
        web3j = web3jAndService.getWeb3j();
        web3jService = web3jAndService.getWeb3jService();
    }
}
