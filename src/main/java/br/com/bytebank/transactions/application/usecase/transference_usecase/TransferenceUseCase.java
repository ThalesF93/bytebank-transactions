package br.com.bytebank.transactions.application.usecase.transference_usecase;

import br.com.bytebank.transactions.infrastructure.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.TransactionResponseDTO;

import java.util.UUID;

public interface TransferenceUseCase {
    TransactionResponseDTO execute(UUID idempotencyKey, TransferenceRequestDTO dto);
}
