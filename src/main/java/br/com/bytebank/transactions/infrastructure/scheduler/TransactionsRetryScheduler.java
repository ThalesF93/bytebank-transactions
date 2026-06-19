package br.com.bytebank.transactions.infrastructure.scheduler;


import br.com.bytebank.transactions.infrastructure.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.database.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.database.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionsRetryScheduler {

    private static final int MAX_ATTEMPTS = 5;

    private final TransactionRepository transactionRepository;
    private final PendingTransactionRepository pendingTransactionRepository;
    private final AccountClient accountClient;

    @Scheduled(fixedDelayString = "${bytebank.transactions.retry.transfer-delay-ms}")
    public void retryPendingTransfer() {
        log.info("Running retryPendingTransfer scheduler...");
        retryPendingOperations(
                OperationType.TRANSFER,
                pending -> {
                    if (pending.getFailureReason() == FailureReason.DEBIT_FAILED) {
                        accountClient.debit(
                                new WithdrawRequestDTO(pending.getOriginAccountId(), pending.getAmount())
                        );
                    }
                    accountClient.credit(
                            new DepositRequestDTO(pending.getDestinationAccountId(), pending.getAmount())
                    );
                }
        );
    }

    @Scheduled(fixedDelayString = "${bytebank.transactions.retry.deposit-delay-ms}")
    public void retryPendingDeposit() {
        log.info("Running retryPendingDeposit scheduler...");
        retryPendingOperations(
                OperationType.DEPOSIT,
                pending -> accountClient.credit(
                        new DepositRequestDTO(pending.getOriginAccountId(), pending.getAmount())
                )
        );
    }

    @Scheduled(fixedDelayString = "${bytebank.transactions.retry.withdraw-delay-ms}")
    public void retryPendingWithdraw() {
        log.info("Running retryPendingWithdraw scheduler...");
        retryPendingOperations(
                OperationType.WITHDRAW,
                pending -> accountClient.debit(
                        new WithdrawRequestDTO(pending.getOriginAccountId(), pending.getAmount())
                )
        );
    }


    private void retryPendingOperations(OperationType type, Consumer<PendingTransaction> operation) {
        var pendingList = pendingTransactionRepository
                .findByOperationTypeAndProcessedFalse(type);

        if (pendingList.isEmpty()) {
            log.info("No pending {} found.", type);
            return;
        }

        for (PendingTransaction pending : pendingList) {
            try {
                operation.accept(pending);
                markAsCompleted(pending);
            } catch (FeignException e) {
                handleRetryFailure(pending);
            }
        }
    }


    private void markAsCompleted(PendingTransaction pending) {
        transactionRepository.findById(pending.getSourceTransaction().getId())
                .ifPresent(t -> {
                    t.setStatus(TransactionStatus.COMPLETED);
                    transactionRepository.save(t);
                });
        pending.setProcessed(true);
        pending.setTransactionStatus(TransactionStatus.COMPLETED);
        pendingTransactionRepository.save(pending);
        log.info("Retry succeeded. pendingId={}", pending.getId());
    }

    private void handleMaxAttempts(PendingTransaction pendingOpening) {
        if (pendingOpening.getOperationType() == OperationType.TRANSFER
                && pendingOpening.getFailureReason() == FailureReason.CREDIT_FAILED) {
            try {
                accountClient.credit(
                        new DepositRequestDTO(
                                pendingOpening.getOriginAccountId(),
                                pendingOpening.getAmount()
                        )
                );
                log.info("Rollback succeeded. Refunded origin account. pendingId={}", pendingOpening.getId());
            } catch (FeignException e) {
                log.error("Rollback failed. Manual intervention required. pendingId={}", pendingOpening.getId());
                return;
            }
        }
        pendingOpening.setTransactionStatus(TransactionStatus.FAILED);
        pendingOpening.setProcessed(true);
        transactionRepository.findById(pendingOpening.getSourceTransaction().getId())
                .ifPresent(t -> {
                    t.setStatus(TransactionStatus.FAILED);
                    transactionRepository.save(t);
                });
        log.error("Max attempts reached. Marking as FAILED. pendingId={}", pendingOpening.getId());
    }

    private void handleRetryFailure(PendingTransaction pending) {
        pending.setAttempts(pending.getAttempts() + 1);
        if (pending.getAttempts() > MAX_ATTEMPTS) {
            handleMaxAttempts(pending);
        }
        pendingTransactionRepository.save(pending);
    }
}
