package br.com.bytebank.transactions.infrastructure.openfeign.dtos.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDTO(
        UUID accountId,

        UUID clientId,

        String agencyNumber,

        BigDecimal balance
){}

