package br.com.bytebank.transactions.domain.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "API Pattern for Errors")
public record ErrorResponse(

        @Schema(description = "Internal Message Error", example = "CUSTOMER_NOT_FOUND")
        String code,

        @Schema(description = "Message describing the error", example = "Customer João not found")
        String message,

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "Source Endpoint", example = "/api/customers/42")
        String path,

        @Schema(description = "Occurred time", example = "2026-05-20T18:00:00Z")
        Instant timestamp
) {
    public static ErrorResponse of(String code, String message, int status, String path) {
        return new ErrorResponse(code, message, status, path, Instant.now());
    }
}
