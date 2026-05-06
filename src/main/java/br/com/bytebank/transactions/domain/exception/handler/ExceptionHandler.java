package br.com.bytebank.transactions.domain.exception.handler;
import br.com.bytebank.transactions.domain.exception.InsufficientBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
}
