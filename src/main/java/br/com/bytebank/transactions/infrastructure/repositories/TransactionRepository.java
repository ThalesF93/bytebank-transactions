package br.com.bytebank.transactions.infrastructure.repositories;

import br.com.bytebank.transactions.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByOriginAccountId(UUID originAccountId);

    List<Transaction> findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(
            UUID originAccountId,
            UUID targetAccountId
    );
}
