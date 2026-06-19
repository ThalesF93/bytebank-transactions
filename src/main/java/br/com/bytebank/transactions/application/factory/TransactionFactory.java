package br.com.bytebank.transactions.application.factory;

import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.AccountOperationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionFactory {


    public PendingTransaction createPendingTransaction(Transaction transaction, FailureReason failureReason) {
        return PendingTransaction.builder()
                .sourceTransaction(transaction)
                .originAccountId(transaction.getOriginAccountId())
                .destinationAccountId(transaction.getTargetAccountId())
                .amount(transaction.getAmount())
                .dateTime(transaction.getDateTime())
                .processed(false)
                .operationType(transaction.getType())
                .attempts(1)
                .transactionStatus(TransactionStatus.PENDING)
                .failureReason(failureReason)
                .build();
    }

    @NonNull
    public Transaction createTransactionEntity(AccountOperationRequest requestDTO, OperationType type, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setOriginAccountId(requestDTO.accountId());;
        transaction.setType(type);
        transaction.setAmount(requestDTO.amount());
        transaction.setDateTime(LocalDateTime.now());
        transaction.setStatus(status);
        return transaction;
    }
}
