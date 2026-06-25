package br.com.bytebank.transactions.application.usecase.impl;

import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.usecase.WithdrawUseCase;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.contract.IdempotencyContract;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.WithdrawResponseDTO;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WithdrawUseCaseImpl implements WithdrawUseCase {

    private final TransactionRepositoryDomain transactionRepository;
    private final IdempotencyContract cacheValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionFactory transactionFactory;
    private final TransactionValidator validator;

    @Override
    public WithdrawResponseDTO execute(UUID idempotencyKey, WithdrawRequestDTO requestDTO) {
        validator.amountValidation(requestDTO.amount());

        String cacheKey = "idempotency:withdraw:" + idempotencyKey;
        Object cached = cacheValidator.get(cacheKey);

        if (cached != null) {
            log.info("Duplicate withdraw detected. idempotencyKey={}", idempotencyKey);
            return cacheValidator.fromIdempotencyCache(cacheKey, WithdrawResponseDTO.class);
        }

        Transaction transaction = transactionFactory.createTransactionEntity(requestDTO, OperationType.WITHDRAW, TransactionStatus.PENDING);
        transactionRepository.save(transaction);

            eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));

        var response = WithdrawResponseDTO.response(transaction);

        cacheValidator.toIdempotencyCache(cacheKey, response);

        return response;

    }
}
