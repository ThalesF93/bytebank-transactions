package br.com.bytebank.transactions.infrastructure.api.dtos.client.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO with Account data received by Feign Client")
public record AccountResponseDTO(

        @Schema(description = "Account id")
        UUID accountId,

        @Schema(description = "Customer ID")
        UUID clientId,

        @Schema(description = "Agency number")
        String agencyNumber,

        @Schema(description = "Account balance")
        BigDecimal balance
){}

