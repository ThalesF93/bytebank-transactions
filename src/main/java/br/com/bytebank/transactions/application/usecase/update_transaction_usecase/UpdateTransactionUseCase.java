package br.com.bytebank.transactions.application.usecase.update_transaction_usecase;

import br.com.bytebank.transactions.domain.enums.TransactionStatus;

import java.util.UUID;

public interface UpdateTransactionUseCase {

    void execute(UUID id, TransactionStatus status);
}
