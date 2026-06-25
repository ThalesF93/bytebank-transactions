package br.com.bytebank.transactions.application.usecase.impl;

import br.com.bytebank.transactions.application.factory.OperationExecutor;
import br.com.bytebank.transactions.application.usecase.FraudCallBackUseCase;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.dtos.requests.FraudServiceRequestDTO;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.InvalidFraudScoreException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.OperationTypeNoneExistingException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.TransactionException;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudCallBackUSeCaseImpl implements FraudCallBackUseCase {

    private final TransactionRepositoryDomain transactionRepositoryDomain;
    private final ApplicationEventPublisher eventPublisher;
    private final OperationExecutor executor;

    @Override
    public void execute(FraudServiceRequestDTO dto) {
        var transaction = transactionRepositoryDomain.findById(dto.transactionID()).orElseThrow(
                () -> new TransactionException(dto.transactionID()));

        switch (dto.score()) {
            case LOW -> executeLowRiskOperation(transaction);
            case MEDIUM -> executeMediumRiskOperation(transaction);
            case HIGH -> executor.blockTransaction(transaction);
            default -> throw new InvalidFraudScoreException(dto.score());
        }
    }

    private void executeLowRiskOperation(Transaction transaction){
        switch (transaction.getType()) {
            case DEPOSIT -> executor.executeDeposit(transaction);
            case WITHDRAW -> executor.executeWithdraw(transaction);
            case TRANSFER -> executor.executeTransfer(transaction);
            default -> throw new OperationTypeNoneExistingException(transaction.getType());
        }
    }

    private void executeMediumRiskOperation(Transaction transaction){
        transaction.setStatus(TransactionStatus.PENDING_CONFIRMATION);
        transactionRepositoryDomain.save(transaction);
        eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));
    }
}