package br.com.bytebank.transactions.api.dtos.client.responses;

import java.io.Serializable;
import java.util.UUID;

public record CustomerClientResponseDTO(
        UUID id,

        String name,

        String email
) implements Serializable {
}
