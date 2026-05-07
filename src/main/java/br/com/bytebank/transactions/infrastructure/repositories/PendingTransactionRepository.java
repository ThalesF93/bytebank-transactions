package br.com.bytebank.transactions.infrastructure.repositories;

import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PendingTransactionRepository extends JpaRepository<PendingTransaction, UUID> {
}
