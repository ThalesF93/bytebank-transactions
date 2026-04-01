package br.com.bytebank.transactions.openfeign.dtos.requests;

import java.util.UUID;

public record AccountRequestDTO(
        UUID customerId
) {
}
