package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;

import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class OperationTypeNoneExistingException extends DefaultException {
    public OperationTypeNoneExistingException(OperationType operationType) {
        super("OPERATION_NOT_VALID", String.format("Operation of type '%s' not permitted", operationType.toString()) , HttpStatus.BAD_REQUEST);
    }
}
