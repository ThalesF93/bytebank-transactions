package br.com.bytebank.transactions.repositories;

import br.com.bytebank.transactions.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
