package io.keyko.monitoring.agent.core.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Transaction with hash not found")
public class TransactionNotFoundEndpointException extends RuntimeException {
}
