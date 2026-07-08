package br.com.bytebank.transactions.domain.repository;

import br.com.bytebank.transactions.domain.entity.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryDomain {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    List<Transaction> findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(
            UUID originAccountId,
            UUID targetAccountId
    );

}
