package br.com.bytebank.transactions.infrastructure.repositories;

import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PendingTransactionRepository extends JpaRepository<PendingTransaction, UUID> {

    List<PendingTransaction> findByOperationTypeAndProcessedFalse(OperationType operationType);


}
