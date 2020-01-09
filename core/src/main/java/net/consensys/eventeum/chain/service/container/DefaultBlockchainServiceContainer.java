package net.consensys.eventeum.chain.service.container;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
