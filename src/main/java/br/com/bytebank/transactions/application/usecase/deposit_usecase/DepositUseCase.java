package br.com.bytebank.transactions.application.usecase.deposit_usecase;

import br.com.bytebank.transactions.infrastructure.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.DepositResponseDTO;

import java.util.UUID;

public interface DepositUseCase {

    DepositResponseDTO execute(UUID idempotencyKey , DepositRequestDTO requestDTO);
}
