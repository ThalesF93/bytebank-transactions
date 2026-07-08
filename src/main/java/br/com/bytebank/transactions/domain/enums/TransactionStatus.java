package br.com.bytebank.transactions.domain.enums;

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    PENDING_CONFIRMATION,
    BLOCKED
}
