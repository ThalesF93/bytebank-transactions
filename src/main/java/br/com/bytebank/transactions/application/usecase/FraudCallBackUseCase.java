package br.com.bytebank.transactions.application.usecase;

import br.com.bytebank.transactions.infrastructure.dtos.requests.FraudServiceRequestDTO;

public interface FraudCallBackUseCase {

    void execute(FraudServiceRequestDTO dto);
}
