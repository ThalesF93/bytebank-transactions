package br.com.bytebank.transactions.application.usecase.impl;

import br.com.bytebank.transactions.application.factory.OperationExecutor;
import br.com.bytebank.transactions.application.usecase.FraudCallBackUseCase;
import br.com.bytebank.transactions.domain.contract.AccountClientContract;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.FraudNotificationEvent;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.FraudScoreEvent;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.InvalidFraudScoreException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.OperationTypeNoneExistingException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.TransactionException;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudCallBackUSeCaseImpl implements FraudCallBackUseCase {

    private final TransactionRepositoryDomain transactionRepositoryDomain;
    private final ApplicationEventPublisher eventPublisher;
    private final OperationExecutor executor;
    private final AccountClientContract accountClient;

    @Override
    @Transactional
    public void execute(FraudScoreEvent dto) {
        var transaction = transactionRepositoryDomain.findById(dto.transactionId()).orElseThrow(
                () -> new TransactionException(dto.transactionId()));
        log.info("Starting bank operations: ");
        switch (dto.score()) {
            case LOW -> executeLowRiskOperation(transaction);
            case MEDIUM -> executeMediumRiskOperation(transaction);
            case HIGH -> {
                executor.blockTransaction(transaction);
                log.info("Transaction id={} BLOCKED due to high fraud risk", transaction.getId());
            }
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
        log.info("Transaction id={}, risk LOW, with type={}, succeeded", transaction.getId(), transaction.getType());
    }

    private void executeMediumRiskOperation(Transaction transaction){
        transactionRepositoryDomain
                .findByOriginAccountIdAndStatus(transaction.getOriginAccountId(), TransactionStatus.PENDING_CONFIRMATION)
                .ifPresent(previous -> {
                    previous.setStatus(TransactionStatus.BLOCKED);
                    transactionRepositoryDomain.save(previous);
                });

        transaction.setStatus(TransactionStatus.PENDING_CONFIRMATION);
        transactionRepositoryDomain.save(transaction);
        var customer = accountClient.findCustomerByAccountId(transaction.getOriginAccountId());
        log.info("Customer data: id={} name={} phone={} email={}",
                customer.id(), customer.name(), customer.phone(), customer.email());
        eventPublisher.publishEvent(new FraudNotificationEvent(
                transaction.getId(), customer.name(), customer.phone(), customer.email()
        ));
        log.info("Transaction id={}, risk MEDIUM with type={}, succeeded", transaction.getId(), transaction.getType());
    }
}