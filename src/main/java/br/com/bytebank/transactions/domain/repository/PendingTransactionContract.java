package br.com.bytebank.transactions.domain.repository;

import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.enums.OperationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PendingTransactionContract {
    void save(PendingTransaction transaction);
    Optional<PendingTransaction> findById(UUID id);
    List<PendingTransaction> findByOperationTypeAndProcessedFalse(OperationType operationType);
}
