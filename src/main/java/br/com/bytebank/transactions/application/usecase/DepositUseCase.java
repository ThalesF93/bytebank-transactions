package br.com.bytebank.transactions.application.usecase;

import br.com.bytebank.transactions.infrastructure.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.DepositResponseDTO;

import java.util.UUID;

public interface DepositUseCase {

    DepositResponseDTO execute(UUID idempotencyKey , DepositRequestDTO requestDTO);
}
