package br.com.bytebank.transactions.domain.enums;

public enum FailureReason {

    DEBIT_FAILED,
    CREDIT_FAILED,
    TARGET_ACCOUNT_NOT_FOUND,
    ORIGIN_ACCOUNT_NOT_FOUND
}
