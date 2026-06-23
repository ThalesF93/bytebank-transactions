package br.com.bytebank.transactions.application.usecase.deposit_usecase;

import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.validator.CacheValidator;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.infrastructure.database.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.publisher.TransactionKafkaPublisher;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositUseCaseImpl implements DepositUseCase{

    private final PendingTransactionRepository pendingTransactionRepository;
    private final TransactionRepositoryDomain transactionRepository;
    private final AccountClient accountClient;
    private final CacheValidator cacheValidator;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TransactionKafkaPublisher kafkaPublisher;
    private final TransactionFactory transactionFactory;
    private final TransactionValidator validator;

    @Override
    public DepositResponseDTO execute(UUID idempotencyKey, DepositRequestDTO requestDTO) {
        validator.amountValidation(requestDTO.amount());

        String cacheKey = "idempotency:deposit:" + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Duplicate deposit detected. idempotencyKey={}", idempotencyKey);
            return cacheValidator.fromIdempotencyCache(cacheKey, DepositResponseDTO.class);
        }

        Transaction transaction = transactionFactory.createTransactionEntity(requestDTO, OperationType.DEPOSIT, TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        try {
            accountClient.credit(new DepositRequestDTO(requestDTO.accountId(), requestDTO.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
            log.info("Deposit succeeded. accountId={}, value={}", requestDTO.accountId(), requestDTO.amount());
            transactionRepository.save(transaction);
            kafkaPublisher.onTransactionCreated(new TransactionCreatedDomainEvent(transaction));

        } catch (FeignException e) {
            transaction.setStatus(TransactionStatus.PENDING);
            PendingTransaction pendingTransaction = transactionFactory.createPendingTransaction(transaction, FailureReason.CREDIT_FAILED);
            pendingTransactionRepository.save(pendingTransaction);
        }

        var response = DepositResponseDTO.response(transaction);
        cacheValidator.toIdempotencyCache(cacheKey, response);

        return response;
    }
}
