package br.com.bytebank.transactions.infrastructure.openfeign.dtos.requests;

import java.util.UUID;

public record AccountRequestDTO(
        UUID customerId
) {
}
