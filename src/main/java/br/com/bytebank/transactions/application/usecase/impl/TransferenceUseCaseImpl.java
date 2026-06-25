package br.com.bytebank.transactions.application.usecase.impl;


import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.usecase.TransferenceUseCase;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.contract.IdempotencyContract;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.TransactionResponseDTO;
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
        log.info("Transference event published. Transaction ID={}", transaction.getId());
        var response = TransactionResponseDTO.transactionPendingResponse(transaction);

        cacheValidator.toIdempotencyCache(cacheKey, response);

        return response;
    }


}
