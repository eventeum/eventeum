package net.consensys.eventeum.endpoint;

import lombok.AllArgsConstructor;
import net.consensys.eventeum.constant.Constants;
import net.consensys.eventeum.dto.transaction.TransactionIdentifier;
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
     * @param hash the transaction hash
     * @param nodeName the name of the node that should be monitored for the transaction
     * @param response the http response
     */
    @RequestMapping(value="/{hash}", method = RequestMethod.POST)
    public void monitorTransaction(@PathVariable String hash,
                                   @RequestParam(required = false) String nodeName,
                                   HttpServletResponse response) {

        if (nodeName == null) {
            nodeName = Constants.DEFAULT_NODE_NAME;
        }

        final TransactionIdentifier txId = new TransactionIdentifier(hash, nodeName);
        monitoringService.registerTransactionToMonitor(txId);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
    }

    /**
     * Stops monitoring a transaction with the specfied hash
     *
     * @param @param hash the transaction hash
     * @param nodeName the name of the node where the transaction is being monitored
     * @param response the http response
     */
    @RequestMapping(value="/{hash}", method = RequestMethod.DELETE)
    public void stopMonitoringTransaction(@PathVariable String hash,
                                          @RequestParam(required = false) String nodeName,
                                          HttpServletResponse response) {

        if (nodeName == null) {
            nodeName = Constants.DEFAULT_NODE_NAME;
        }

        try {
            monitoringService.stopMonitoringTransaction(new TransactionIdentifier(hash, nodeName));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch(NotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new TransactionNotFoundEndpointException();
        }
    }
}