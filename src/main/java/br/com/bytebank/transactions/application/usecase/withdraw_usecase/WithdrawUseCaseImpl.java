package br.com.bytebank.transactions.application.usecase.withdraw_usecase;

import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.validator.CacheValidator;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.WithdrawResponseDTO;
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
@Slf4j
@RequiredArgsConstructor
public class WithdrawUseCaseImpl implements WithdrawUseCase{

    private final PendingTransactionRepository pendingTransactionRepository;
    private final TransactionRepositoryDomain transactionRepository;
    private final AccountClient accountClient;
    private final CacheValidator cacheValidator;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TransactionKafkaPublisher kafkaPublisher;
    private final TransactionFactory factory;
    private final TransactionValidator validator;

    @Override
    public WithdrawResponseDTO withdraw(UUID idempotencyKey, WithdrawRequestDTO requestDTO) {
        validator.amountValidation(requestDTO.amount());

        String cacheKey = "idempotency:withdraw:" + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Duplicate withdraw detected. idempotencyKey={}", idempotencyKey);
            return cacheValidator.fromIdempotencyCache(cacheKey, WithdrawResponseDTO.class);
        }

        Transaction transaction = factory.createTransactionEntity(requestDTO, OperationType.WITHDRAW, TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        try {
            accountClient.debit(new WithdrawRequestDTO(requestDTO.accountId(), requestDTO.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
            log.info("Withdraw succeeded. accountId={}, value={}", requestDTO.accountId(), requestDTO.amount());
            kafkaPublisher.onTransactionCreated(new TransactionCreatedDomainEvent(transaction));
            transactionRepository.save(transaction);


        } catch (FeignException e) {

            PendingTransaction pendingTransaction = factory.createPendingTransaction(transaction, FailureReason.DEBIT_FAILED);
            pendingTransactionRepository.save(pendingTransaction);
        }

        var response = WithdrawResponseDTO.response(transaction);

        cacheValidator.toIdempotencyCache(cacheKey, response);

        return response;

    }
}
