package br.com.bytebank.transactions.infrastructure.database;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class TransactionRepositoryImpl implements TransactionRepositoryDomain {

    private final TransactionRepository transactionRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<Transaction> findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(UUID originAccountId, UUID targetAccountId) {
        return transactionRepository.findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(originAccountId, targetAccountId);
    }
}
