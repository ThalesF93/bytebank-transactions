package br.com.bytebank.transactions.application.factory;


import br.com.bytebank.transactions.domain.contract.AccountClientContract;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.PendingTransactionContract;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.AccountServiceUnavailableException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.ServiceUnavailableException;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class OperationExecutor {

    private final TransactionRepositoryDomain transactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountClientContract accountClient;
    private final PendingTransactionContract pendingTransactionContract;
    private final TransactionFactory factory;

    public void executeDeposit(Transaction transaction ){
        if (isAccountServiceUnavailable(() -> accountClient.credit(transaction.getOriginAccountId(), transaction.getAmount()))) {
            markAsPending(transaction, FailureReason.DEBIT_FAILED);
        }
        else {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));
        }

    }

    public void executeWithdraw(Transaction transaction ){
        if (isAccountServiceUnavailable(() -> accountClient.debit(transaction.getOriginAccountId(), transaction.getAmount()))) {
            markAsPending(transaction, FailureReason.DEBIT_FAILED);
        }

        else {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));
        }

    }

    public void blockTransaction(Transaction transaction){
        transaction.setStatus(TransactionStatus.BLOCKED);
        transactionRepository.save(transaction);
        eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));
    }


    public void executeTransfer(Transaction transaction) {

        if (isAccountServiceUnavailable(() -> accountClient.debit(transaction.getOriginAccountId(), transaction.getAmount()))) {
            markAsPending(transaction, FailureReason.DEBIT_FAILED);
            return;
        }

        if (isAccountServiceUnavailable(() -> accountClient.credit(transaction.getTargetAccountId(), transaction.getAmount()))) {
            markAsPending(transaction, FailureReason.CREDIT_FAILED);

        }

        else {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));
            log.info("Transference succeeded. originAccountId={}, destinationAccountId={}, value={}",
                    transaction.getOriginAccountId(), transaction.getTargetAccountId(), transaction.getAmount());
        }

    }

    private boolean isAccountServiceUnavailable(Runnable operation) {
        try {
            operation.run();
            return false;
        } catch (AccountServiceUnavailableException | ServiceUnavailableException e) {
            log.warn("Account service unavailable: {}", e.getMessage());
            return true;
        }
    }

    private void markAsPending(Transaction transaction, FailureReason reason) {
        transaction.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        pendingTransactionContract.save(factory.createPendingTransaction(transaction, reason));

        log.info("Transference pending. originAccountId={}, destinationAccountId={}, value={}",
                transaction.getOriginAccountId(), transaction.getTargetAccountId(), transaction.getAmount());

    }
}
