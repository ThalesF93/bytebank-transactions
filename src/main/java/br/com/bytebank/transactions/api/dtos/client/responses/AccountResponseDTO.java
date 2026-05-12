package br.com.bytebank.transactions.api.dtos.client.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDTO(
        UUID accountId,

        UUID clientId,

        String agencyNumber,

        BigDecimal balance
){}

