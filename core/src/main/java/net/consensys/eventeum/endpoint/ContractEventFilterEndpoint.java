package net.consensys.eventeum.endpoint;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.endpoint.response.AddEventFilterResponse;
import net.consensys.eventeum.service.FilterNotFoundException;
import net.consensys.eventeum.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * A REST endpoint for adding a removing event filters.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@RestController
@RequestMapping(value = "/api/rest/v1/event-filter")
public class ContractEventFilterEndpoint {

    private SubscriptionService filterService;

    @Autowired
    public ContractEventFilterEndpoint(SubscriptionService filterService) {
        this.filterService = filterService;
    }

    /**
     * Adds an event filter with the specification described in the ContractEventFilter.
     *
     * @param eventFilter the event filter to add
     * @param response the http response
     */
    @RequestMapping(method = RequestMethod.POST)
    public AddEventFilterResponse addEventFilter(@RequestBody ContractEventFilter eventFilter,
                                                 HttpServletResponse response) {

        final ContractEventFilter registeredFilter = filterService.registerContractEventFilter(eventFilter);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);

        return new AddEventFilterResponse(registeredFilter.getId());
    }

    /**
     * Deletes an event filter with the corresponding filter id.
     *
     * @param filterId the filterId to delete
     * @param response the http response
     */
    @RequestMapping(value="/{filterId}", method = RequestMethod.DELETE)
    public void removeEventFilter(@PathVariable String filterId,
                               HttpServletResponse response) {

        try {
            filterService.unregisterContractEventFilter(filterId, true);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch(FilterNotFoundException e) {
            //Rethrow endpoint exception with response information
            throw new FilterNotFoundEndpointException();
        }
    }
}