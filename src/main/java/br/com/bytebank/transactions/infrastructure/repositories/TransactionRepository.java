package br.com.bytebank.transactions.infrastructure.repositories;

import br.com.bytebank.transactions.domain.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
