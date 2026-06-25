package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;

import br.com.bytebank.transactions.domain.enums.FraudScore;
import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class InvalidFraudScoreException extends DefaultException {
    public InvalidFraudScoreException(FraudScore score) {
        super("INVALID_SCORE", String.format("Score type %s is unknown", score.toString()), HttpStatus.CONFLICT);
    }
}
