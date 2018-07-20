package net.consensys.eventeum.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Filter with id not found")
public class FilterNotFoundEndpointException extends RuntimeException {
}
