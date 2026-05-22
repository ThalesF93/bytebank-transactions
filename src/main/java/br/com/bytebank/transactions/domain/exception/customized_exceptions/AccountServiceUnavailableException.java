package br.com.bytebank.transactions.domain.exception.customized_exceptions;

import br.com.bytebank.transactions.domain.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class AccountServiceUnavailableException extends DefaultException {
    public AccountServiceUnavailableException() {
        super("SERVICE_UNAVAILABLE", "Account service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
