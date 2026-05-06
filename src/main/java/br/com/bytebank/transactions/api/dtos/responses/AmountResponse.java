package br.com.bytebank.transactions.api.dtos.responses;

import java.math.BigDecimal;

public record AmountResponse(
        BigDecimal amount
) {
}
