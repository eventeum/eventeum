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

package net.consensys.eventeum.chain.service.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DefaultBlockchainServiceContainer implements ChainServicesContainer {

    private Map<String, NodeServices> nodeServicesMap;

    public DefaultBlockchainServiceContainer(List<NodeServices> nodeServices) {
        nodeServicesMap = new HashMap<>();
        nodeServices.forEach(ns -> nodeServicesMap.put(ns.getNodeName(), ns));
    }

    @Override
    public NodeServices getNodeServices(String nodeName) {
        return nodeServicesMap.get(nodeName);
    }

    @Override
    public List<String> getNodeNames() {
        return new ArrayList(nodeServicesMap.keySet());
    }
}
