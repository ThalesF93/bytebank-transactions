package br.com.bytebank.transactions.domain.enums;

import java.math.BigDecimal;

public enum FeeType {
    CURRENT_ACCOUNT_TRANSFER(new BigDecimal("0.05") ),
    SAVINGS_ACCOUNT_TRANSFER(new BigDecimal("0.00") ),
    GOLD_ACCOUNT_TRANSFER(new BigDecimal("0.15") ),
    WITHDRAW(new BigDecimal("0.02") ),
    PIX(new BigDecimal("0.01"));


    private final BigDecimal rate;

    FeeType(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getRate() {
        return rate;
    }
}
