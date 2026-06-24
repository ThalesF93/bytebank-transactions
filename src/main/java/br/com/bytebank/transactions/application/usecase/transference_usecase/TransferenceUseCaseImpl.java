package br.com.bytebank.transactions.application.usecase.transference_usecase;


import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.api.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.infrastructure.database.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.AccountServiceUnavailableException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.ServiceUnavailableException;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.publisher.TransactionKafkaPublisher;
import br.com.bytebank.transactions.infrastructure.messaging.rabbitmq.TransactionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class TransferenceUseCaseImpl implements TransferenceUseCase {

    private final PendingTransactionRepository pendingTransactionRepository;
    private final TransactionRepositoryDomain transactionRepository;
    private final AccountClient accountClient;
    private final CacheValidator cacheValidator;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TransactionKafkaPublisher kafkaPublisher;
    private final TransactionFactory factory;
    private final TransactionValidator validator;
    private final TransactionEventPublisher eventPublisher;

    public TransactionResponseDTO execute(UUID idempotencyKey, TransferenceRequestDTO dto){

        String cacheKey = "idempotency:transference:" + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Duplicate transference detected. idempotencyKey={}", idempotencyKey);
            return cacheValidator.fromIdempotencyCache(cacheKey, TransactionResponseDTO.class);
        }

        validator.validatingTransference(dto);

        AccountResponseDTO originAccount;
        AccountResponseDTO destinationAccount;

        originAccount = validator.getAccountForTransaction(dto.originAccountId());
        destinationAccount = validator.getAccountForTransaction(dto.destinationAccountId());

        Transaction transaction = factory.createTransactionEntity(dto, OperationType.TRANSFER, TransactionStatus.PROCESSING);
        transaction.setTargetAccountId(dto.destinationAccountId());
        transactionRepository.save(transaction);

        var response = executeTransfer(transaction, dto, originAccount, destinationAccount);

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
