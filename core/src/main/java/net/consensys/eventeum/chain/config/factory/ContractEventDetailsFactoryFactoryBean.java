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
import net.consensys.eventeum.chain.converter.EventParameterConverter;
import net.consensys.eventeum.chain.factory.ContractEventDetailsFactory;
import net.consensys.eventeum.chain.factory.DefaultContractEventDetailsFactory;
import net.consensys.eventeum.chain.settings.Node;
import org.springframework.beans.factory.FactoryBean;
import org.web3j.abi.datatypes.Type;

@Data
public class ContractEventDetailsFactoryFactoryBean
        implements FactoryBean<ContractEventDetailsFactory> {

    EventParameterConverter<Type> parameterConverter;
    Node node;
    String nodeName;

    @Override
    public ContractEventDetailsFactory getObject() throws Exception {
        return new DefaultContractEventDetailsFactory(
                parameterConverter, node, nodeName);
    }

    @Override
    public Class<?> getObjectType() {
        return ContractEventDetailsFactory.class;
    }
}
