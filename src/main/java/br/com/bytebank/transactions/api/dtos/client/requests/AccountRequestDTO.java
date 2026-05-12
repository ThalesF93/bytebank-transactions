package br.com.bytebank.transactions.api.dtos.client.requests;

import java.util.UUID;

public record AccountRequestDTO(
        UUID customerId
) {
}
