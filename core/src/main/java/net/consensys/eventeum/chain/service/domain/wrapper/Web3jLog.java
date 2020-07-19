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

package net.consensys.eventeum.chain.service.domain.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.consensys.eventeum.chain.service.domain.Log;
import net.consensys.eventeum.utils.ModelMapperFactory;
import org.modelmapper.ModelMapper;

import java.math.BigInteger;
import java.util.List;

/**
 * A Log that is constructed from a Web3j Log object.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@NoArgsConstructor
public class Web3jLog implements Log {

    private boolean removed;
    private BigInteger logIndex;
    private BigInteger transactionIndex;
    private String transactionHash;
    private String blockHash;
    private BigInteger blockNumber;
    private String address;
    private String data;
    private String type;
    private List<String> topics;

    public Web3jLog(org.web3j.protocol.core.methods.response.Log web3jLog) {

        final ModelMapper modelMapper = ModelMapperFactory.getInstance().createModelMapper();
        modelMapper.map(web3jLog, this);
    }
}
