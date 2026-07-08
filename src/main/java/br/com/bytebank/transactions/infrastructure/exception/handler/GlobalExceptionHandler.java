package br.com.bytebank.transactions.infrastructure.exception.handler;

import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import br.com.bytebank.transactions.infrastructure.exception.dto.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DefaultException.class)
    public ResponseEntity<ErrorResponse> handleDefaultException(DefaultException e, HttpServletRequest request){
        log.warn("Error: {} - {}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(ErrorResponse.of(e.getCode(), e.getMessage(), e.getStatus().value(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message, 400, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected Error", 500, request.getRequestURI()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, HttpServletRequest request) {
        log.error("Feign Client Error", ex);
        int status = ex.status() > 0 ? ex.status() : 500;
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of("FEIGN_CLIENT_ERROR", "Unexpected Error", status, request.getRequestURI()));
    }


}
