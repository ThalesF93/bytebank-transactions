package br.com.bytebank.transactions.infrastructure.database;

import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.repository.PendingTransactionContract;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PendingTransactionRepositoryGateway implements PendingTransactionContract {

    private final PendingTransactionRepository pendingTransactionRepository;

    @Override
    public void save(PendingTransaction transaction) {
        pendingTransactionRepository.save(transaction);
    }

    @Override
    public Optional<PendingTransaction> findById(UUID id) {
        return pendingTransactionRepository.findById(id);
    }

    @Override
    public List<PendingTransaction> findByOperationTypeAndProcessedFalse(OperationType operationType) {
        return pendingTransactionRepository.findByOperationTypeAndProcessedFalse(operationType);
    }
}
