package br.com.bytebank.transactions.application.usecase.withdraw_usecase;

import br.com.bytebank.transactions.infrastructure.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.WithdrawResponseDTO;

import java.util.UUID;

public interface WithdrawUseCase {
    WithdrawResponseDTO execute(UUID idempotencyKey, WithdrawRequestDTO requestDTO);
}
