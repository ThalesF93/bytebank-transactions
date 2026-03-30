package br.com.bytebank.transactions.dtos.responses;

import java.math.BigDecimal;

public record AmountResponse(
        BigDecimal amount
) {
}
