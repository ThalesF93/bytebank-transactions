package br.com.bytebank.transactions.infrastructure.api.dtos.client.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.UUID;

@Schema(description = "Response DTO with customer information")
public record CustomerClientResponseDTO(

        @Schema(description = "Customer`s ID")
        UUID id,

        @Schema(description = "Customer`s Name")
        String name,

        @Schema(description = "Customer`s Email")
        String email
) implements Serializable {
}
