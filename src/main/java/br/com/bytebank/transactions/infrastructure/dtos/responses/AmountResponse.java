package br.com.bytebank.transactions.infrastructure.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Return the balance of an account")
public record AmountResponse(

        @Schema(description = "Balance of an account")
        BigDecimal amount
) {
}
