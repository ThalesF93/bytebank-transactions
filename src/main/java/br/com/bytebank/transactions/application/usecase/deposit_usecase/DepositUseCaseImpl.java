package br.com.bytebank.transactions.application.usecase.deposit_usecase;

import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.contract.IdempotencyContract;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositUseCaseImpl implements DepositUseCase{


    private final TransactionRepositoryDomain transactionRepository;
    private final IdempotencyContract cacheValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionFactory transactionFactory;
    private final TransactionValidator validator;

    @Override
    public DepositResponseDTO execute(UUID idempotencyKey, DepositRequestDTO requestDTO) {
        validator.amountValidation(requestDTO.amount());

        String cacheKey = "idempotency:deposit:" + idempotencyKey;
        Object cached = cacheValidator.get(cacheKey);

        if (cached != null) {
            log.info("Duplicate deposit detected. idempotencyKey={}", idempotencyKey);
            return cacheValidator.fromIdempotencyCache(cacheKey, DepositResponseDTO.class);
        }

        Transaction transaction = transactionFactory.createTransactionEntity(requestDTO, OperationType.DEPOSIT, TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        eventPublisher.publishEvent(new TransactionCreatedDomainEvent(transaction));

        var response = DepositResponseDTO.response(transaction);
        cacheValidator.toIdempotencyCache(cacheKey, response);

        return response;
    }
}
