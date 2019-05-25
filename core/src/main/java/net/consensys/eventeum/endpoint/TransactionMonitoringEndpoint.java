package net.consensys.eventeum.endpoint;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.transaction.TransactionIdentifier;
import net.consensys.eventeum.endpoint.response.MonitorTransactionsResponse;
import net.consensys.eventeum.model.TransactionIdentifierType;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.service.TransactionMonitoringService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * A REST endpoint for adding a removing event filters.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@RestController
@RequestMapping(value = "/api/rest/v1/transaction")
@AllArgsConstructor
public class TransactionMonitoringEndpoint {

    private TransactionMonitoringService monitoringService;

    /**
     * Monitors a transaction with the specified hash, on a specific node
     *
     * @param identifier the transaction identifier (hash for now)
     * @param nodeName the name of the node that should be monitored for the transaction
     * @param response the http response
     */
    @RequestMapping(method = RequestMethod.POST)
    public MonitorTransactionsResponse monitorTransactions(@RequestParam(required = false) String identifier,
                                                           @RequestParam(required = false) String nodeName,
                                                           HttpServletResponse response) {

        if (nodeName == null) {
            nodeName = Constants.DEFAULT_NODE_NAME;
        }

        final TransactionMonitoringSpec spec =
                new TransactionMonitoringSpec(TransactionIdentifierType.HASH, identifier, nodeName);
        monitoringService.registerTransactionsToMonitor(spec);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

        return new MonitorTransactionsResponse(spec.getId());
    }

    /**
     * Stops monitoring a transaction with the specfied hash
     *
     * @param @param specId the id of the transaction monitor to remove
     * @param nodeName the name of the node where the transaction is being monitored
     * @param response the http response
     */
    @RequestMapping(value="/{id}", method = RequestMethod.DELETE)
    public void stopMonitoringTransaction(@PathVariable String id,
                                          @RequestParam(required = false) String nodeName,
                                          HttpServletResponse response) {

        try {
            monitoringService.stopMonitoringTransactions(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch(NotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new TransactionNotFoundEndpointException();
        }
    }
}