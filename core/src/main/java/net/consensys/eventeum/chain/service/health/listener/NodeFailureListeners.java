package net.consensys.eventeum.chain.service.health.listener;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
public class NodeFailureListeners {

    List<NodeFailureListener> listeners;
}
