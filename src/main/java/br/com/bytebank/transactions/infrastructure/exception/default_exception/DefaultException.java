package br.com.bytebank.transactions.infrastructure.exception.default_exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class DefaultException extends RuntimeException {

    private final String code;
    private final HttpStatus status;


    public DefaultException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }


    public DefaultException(String message) {
        super(message);
        this.code = null;
        this.status= null;
    }
}
