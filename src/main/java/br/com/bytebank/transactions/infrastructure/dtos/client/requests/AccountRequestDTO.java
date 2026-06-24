package br.com.bytebank.transactions.infrastructure.dtos.client.requests;

import java.util.UUID;

public record AccountRequestDTO(
        UUID customerId
) {
}
