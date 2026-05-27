package br.com.bytebank.transactions.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bytebank.transactions.retry")
public record RetryProperties(
        int maxAttempts,
        long depositDelayMs,
        long withdrawDelayMs,
        long transferDelayMs
) {
}
