package br.com.bytebank.transactions.application.usecase.transference_usecase;


import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.contract.IdempotencyContract;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.AccountServiceUnavailableException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.ServiceUnavailableException;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class TransferenceUseCaseImpl implements TransferenceUseCase {

    private final TransactionRepositoryDomain transactionRepository;
    private final IdempotencyContract cacheValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionFactory transactionFactory;
    private final TransactionValidator validator;

    public TransactionResponseDTO execute(UUID idempotencyKey, TransferenceRequestDTO dto){

        String cacheKey = "idempotency:transference:" + idempotencyKey;
        Object cached = cacheValidator.get(cacheKey);

        if (cached != null) {
            log.info("Duplicate transference detected. idempotencyKey={}", idempotencyKey);
            return cacheValidator.fromIdempotencyCache(cacheKey, TransactionResponseDTO.class);
        }

        validator.validatingTransference(dto);

        AccountResponseDTO originAccount;
        AccountResponseDTO destinationAccount;

        originAccount = validator.getAccountForTransaction(dto.originAccountId());
        destinationAccount = validator.getAccountForTransaction(dto.destinationAccountId());

        Transaction transaction = transactionFactory.createTransactionEntity(dto, OperationType.TRANSFER, TransactionStatus.PENDING);
        transaction.setTargetAccountId(dto.destinationAccountId());
        transactionRepository.save(transaction);

        eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));
        var response = TransactionResponseDTO.transferencePendingResponse(transaction);

        cacheValidator.toIdempotencyCache(cacheKey, response);

        return response;
    }

    private TransactionResponseDTO executeTransfer(
            Transaction transaction,
            TransferenceRequestDTO dto,
            AccountResponseDTO originAccount,
            AccountResponseDTO destinationAccount) {

        if (isAccountServiceUnavailable(() -> accountClient.debit(new WithdrawRequestDTO(originAccount.accountId(), dto.amount())))) {
            return markAsPending(transaction, FailureReason.DEBIT_FAILED, dto);
        }

        if (isAccountServiceUnavailable(() -> accountClient.credit(new DepositRequestDTO(destinationAccount.accountId(), dto.amount())))) {
            return markAsPending(transaction, FailureReason.CREDIT_FAILED, dto);
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        eventPublisher.publishTransferenceCompleted(transaction);
        kafkaPublisher.onTransactionCreated(new TransactionCreatedDomainEvent(transaction));
        log.info("Transference succeeded. originAccountId={}, destinationAccountId={}, value={}",
                dto.originAccountId(), dto.destinationAccountId(), dto.amount());

        return TransactionResponseDTO.transferenceCompletedResponse(transaction);
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

    private TransactionResponseDTO markAsPending(Transaction transaction, FailureReason reason, TransferenceRequestDTO dto) {
        transaction.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        pendingTransactionRepository.save(factory.createPendingTransaction(transaction, reason));

        log.info("Transference pending. originAccountId={}, destinationAccountId={}, value={}",
                dto.originAccountId(), dto.destinationAccountId(), dto.amount());

        return TransactionResponseDTO.transferencePendingResponse(transaction);
    }
}
