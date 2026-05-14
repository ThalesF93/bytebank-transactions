package br.com.bytebank.transactions.infrastructure.config;


import br.com.bytebank.transactions.domain.exception.InsufficientBalanceException;
import br.com.bytebank.transactions.domain.exception.ResourceNotFoundException;
import br.com.bytebank.transactions.domain.exception.ServiceUnavailableException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.BadRequestException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String responseBody = getResponseBody(response);

        return switch (response.status()) {
            case 400 -> new BadRequestException(buildMessage(methodKey, response, responseBody));
            case 404 -> new ResourceNotFoundException(buildMessage(methodKey, response, responseBody));
            case 409 -> handleConflict(methodKey, response, responseBody);
            case 500, 502, 503, 504 -> new ServiceUnavailableException(buildMessage(methodKey, response, responseBody));
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }

    private Exception handleConflict(String methodKey, Response response, String responseBody) {
        String message = buildMessage(methodKey, response, responseBody);

        if (responseBody != null && responseBody.toLowerCase().contains("insufficient")) {
            return new InsufficientBalanceException(message);
        }

        if (responseBody != null && responseBody.toLowerCase().contains("no fallback available")) {
            return new ServiceUnavailableException(message);
        }

        return FeignException.errorStatus(methodKey, response);
    }

    private String buildMessage(String methodKey, Response response, String responseBody) {
        return String.format(
                "Feign call failed. methodKey=%s, status=%d, reason=%s, url=%s, body=%s",
                methodKey,
                response.status(),
                response.reason(),
                response.request() != null ? response.request().url() : "unknown",
                responseBody
        );
    }

    private String getResponseBody(Response response) {
        if (response.body() == null) {
            return "";
        }

        try {
            return new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return "Could not read Feign response body";
        }
    }
}
