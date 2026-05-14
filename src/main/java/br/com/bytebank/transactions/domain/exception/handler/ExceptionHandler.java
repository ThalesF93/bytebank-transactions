package br.com.bytebank.transactions.domain.exception.handler;
import br.com.bytebank.transactions.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleInsufficientBalance(final Throwable exception){

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problemDetail.setTitle("Insufficient Balance for operation");
        problemDetail.setType(URI.create("https://api.coderbank.com.br/errors/conflict"));

        return problemDetail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(final MethodArgumentNotValidException exception){

        Map<String, String> validationErrors = buildValidationErrorResponse(exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation error"
        );

        problemDetail.setTitle("Invalid data");
        problemDetail.setProperty("errors", validationErrors);
        problemDetail.setType(URI.create("https://api.coderbank.com.br/errors/validation"));

        return problemDetail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundException(final Throwable throwable){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                throwable.getMessage()
        );
        problemDetail.setTitle("Account not found");
        problemDetail.setType(URI.create("https://api.coderbank.com.br/errors/notFound"));

        return problemDetail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(TransactionException.class)
    public ProblemDetail handleTransactionException(final Throwable throwable){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                throwable.getMessage()
        );
        problemDetail.setTitle("Transference error");
        problemDetail.setType(URI.create("https://api.coderbank.com.br/errors/notFound"));

        return problemDetail;
    }

    private static Map<String, String> buildValidationErrorResponse(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new HashMap<>();

        exception.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    validationErrors.put(fieldName, errorMessage);
                });
        return validationErrors;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AccountServiceUnavailableException.class)
    public ProblemDetail handleAccountServiceUnavailableException(final Throwable throwable){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                throwable.getMessage()
        );
        problemDetail.setTitle("Account service unavailable");
        problemDetail.setType(URI.create("https://api.coderbank.com.br/errors/notFound"));

        return problemDetail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoFallbackAvailableException.class)
    public ProblemDetail handleNoFallbackAvailable(final Throwable exception){

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External service unavailable or circuit breaker fallback not configured."
        );

        problemDetail.setTitle("External Service Unavailable");
        problemDetail.setType(URI.create("https://api.coderbank.com.br/errors/service-unavailable"));

        return problemDetail;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ServiceUnavailableException.class)
    public ProblemDetail handleServiceUnavailable(final Throwable exception){

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage()
        );

        problemDetail.setTitle("External Service Unavailable");
        problemDetail.setType(URI.create("https://api.coderbank.com.br/errors/service-unavailable"));

        return problemDetail;
    }
}
