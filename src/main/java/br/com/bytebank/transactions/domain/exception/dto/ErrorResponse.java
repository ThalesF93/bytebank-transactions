package br.com.bytebank.transactions.domain.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "API Pattern for Errors")
public record ErrorResponse(

        @Schema(description = "Internal Message Error")
        String code,

        @Schema(description = "Message describing the error")
        String message,

        @Schema(description = "HTTP status code")
        int status,

        @Schema(description = "Source Endpoint")
        String path,

        @Schema(description = "Occurred time")
        Instant timestamp
) {
    public static ErrorResponse of(String code, String message, int status, String path) {
        return new ErrorResponse(code, message, status, path, Instant.now());
    }
}
