package br.com.bytebank.transactions.infrastructure.scheduler;


import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.repositories.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.repositories.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountRetryScheduler {

    private static final int MAX_ATTEMPTS = 5;

    private final TransactionRepository transactionRepository;
    private final PendingTransactionRepository pendingTransactionRepository;
    private final AccountClient accountClient;

    @Scheduled(fixedDelay = 60000)
    public  void retryPendingDeposit(){
        log.info("Running retryPendingDeposit scheduler...");
        List<PendingTransaction> list =
                pendingTransactionRepository.findByOperationTypeAndProcessedFalse(OperationType.DEPOSIT);

        if (list.isEmpty()){
            log.info("No pending deposits found.");
            return;
        }

        for (PendingTransaction pendingOpening : list) {

            try {

                accountClient.credit(new DepositRequestDTO(pendingOpening.getOriginAccountId(), pendingOpening.getAmount()));

                transactionRepository.findById(pendingOpening.getSourceTransaction().getId())
                        .ifPresent(t -> {
                            t.setStatus(TransactionStatus.COMPLETED);
                            transactionRepository.save(t);
                            log.info("Pending deposit retried successfully. pendingId={}, accountId={}, amount={}",
                                    pendingOpening.getId(), pendingOpening.getOriginAccountId(), pendingOpening.getAmount());
                        });

                pendingOpening.setTransactionStatus(TransactionStatus.COMPLETED);
                pendingOpening.setProcessed(true);
                pendingTransactionRepository.save(pendingOpening);

            } catch (FeignException e) {
                pendingOpening.setAttempts(pendingOpening.getAttempts() + 1);

                if (pendingOpening.getAttempts() > MAX_ATTEMPTS) {
                    handleMaxAttempts(pendingOpening);
                }
                pendingTransactionRepository.save(pendingOpening);
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void retryPendingWithdraw(){
        log.info("Running retryPendingWithdraw scheduler...");

        List<PendingTransaction> list =
                pendingTransactionRepository.findByOperationTypeAndProcessedFalse(OperationType.WITHDRAW);

        if (list.isEmpty()){
            log.info("No pending withdraws found.");
            return;
        }

        for (PendingTransaction pendingOpening : list){

            try {

                accountClient.debit(new WithdrawRequestDTO(pendingOpening.getOriginAccountId(), pendingOpening.getAmount()));

                transactionRepository.findById(pendingOpening.getSourceTransaction().getId())
                        .ifPresent(t-> {
                            t.setStatus(TransactionStatus.COMPLETED);
                            transactionRepository.save(t);
                            log.info("Pending withdraw retried successfully. pendingId={}, accountId={}, amount={}",
                                    pendingOpening.getId(), pendingOpening.getOriginAccountId(), pendingOpening.getAmount());
                        });

                pendingOpening.setProcessed(true);
                pendingOpening.setTransactionStatus(TransactionStatus.COMPLETED);
                pendingTransactionRepository.save(pendingOpening);

            } catch (FeignException e) {
                pendingOpening.setAttempts(pendingOpening.getAttempts()+1);

                if (pendingOpening.getAttempts() > MAX_ATTEMPTS) {
                    handleMaxAttempts(pendingOpening);
                }
                pendingTransactionRepository.save(pendingOpening);
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void retryPendingTransference(){
        log.info("Running retryPendingTransference scheduler...");

        List<PendingTransaction> list =
                pendingTransactionRepository.findByOperationTypeAndProcessedFalse(OperationType.TRANSFER);

        if (list.isEmpty()){
            log.info("No pending transference found.");
            return;
        }

        for (PendingTransaction pendingOpening : list){

            try {
                if (pendingOpening.getFailureReason() == FailureReason.DEBIT_FAILED) {
                    accountClient.debit(new WithdrawRequestDTO(pendingOpening.getOriginAccountId(), pendingOpening.getAmount()));
                    log.info("Debit retry succeeded into accountId={}", pendingOpening.getOriginAccountId());
                }

                try {
                    accountClient.credit(new DepositRequestDTO(pendingOpening.getDestinationAccountId(), pendingOpening.getAmount()));
                    transactionRepository.findById(pendingOpening.getSourceTransaction().getId())
                            .ifPresent(t-> {
                                t.setStatus(TransactionStatus.COMPLETED);
                                transactionRepository.save(t);
                                log.info("Pending transference retried successfully. pendingId={}, accountOriginId={}, targetAccountId={} amount={}",
                                        pendingOpening.getId(), pendingOpening.getOriginAccountId(), pendingOpening.getDestinationAccountId(), pendingOpening.getAmount());
                            });
                    pendingOpening.setProcessed(true);
                    pendingOpening.setTransactionStatus(TransactionStatus.COMPLETED);
                    pendingTransactionRepository.save(pendingOpening);

                } catch (FeignException e){
                    pendingOpening.setAttempts(pendingOpening.getAttempts()+1);
                    if (pendingOpening.getAttempts() > MAX_ATTEMPTS) {
                        handleMaxAttempts(pendingOpening);
                    }

                    pendingTransactionRepository.save(pendingOpening);
                }
            } catch (FeignException e) {
                pendingOpening.setAttempts(pendingOpening.getAttempts()+1);

                if (pendingOpening.getAttempts() > MAX_ATTEMPTS) {
                    handleMaxAttempts(pendingOpening);
                }
                pendingTransactionRepository.save(pendingOpening);
            }
        }
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
}
