package net.consensys.eventeum.integration.broadcast.blockchain;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.broadcast.BroadcastException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * A BlockchainEventBroadcaster that broadcasts the events via a http post.
 *
 * The url to post to for block and contract events can be configured via the
 * broadcast.http.contractEvents and broadcast.http.blockEvents properties.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class HttpBlockchainEventBroadcaster implements BlockchainEventBroadcaster {

    private HttpBroadcasterSettings settings;

    private RestTemplate restTemplate;

    public HttpBlockchainEventBroadcaster(HttpBroadcasterSettings settings) {
        this.settings = settings;

        restTemplate = new RestTemplate();
    }

    @Override
    public void broadcastNewBlock(BlockDetails block) {
        final ResponseEntity<Void> response =
                restTemplate.postForEntity(settings.getBlockEventsUrl(), block, Void.class);

        checkForSuccessResponse(response);
    }

    @Override
    public void broadcastContractEvent(ContractEventDetails eventDetails) {
        final ResponseEntity<Void> response =
                restTemplate.postForEntity(settings.getContractEventsUrl(), eventDetails, Void.class);

        checkForSuccessResponse(response);
    }

    private void checkForSuccessResponse(ResponseEntity<Void> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new BroadcastException("Received a %s response when broadcasting via http");
        }
    }
}
