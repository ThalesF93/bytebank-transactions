package br.com.bytebank.transactions.application.usecase;

import br.com.bytebank.transactions.infrastructure.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.WithdrawResponseDTO;

import java.util.UUID;

public interface WithdrawUseCase {
    WithdrawResponseDTO execute(UUID idempotencyKey, WithdrawRequestDTO requestDTO);
}
