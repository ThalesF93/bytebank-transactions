package br.com.bytebank.transactions.infrastructure.database;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByOriginAccountId(UUID originAccountId);

    List<Transaction> findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(
            UUID originAccountId,
            UUID targetAccountId
    );

    Optional<Transaction> findByOriginAccountIdAndStatus(UUID originAccountId, TransactionStatus status);
}
