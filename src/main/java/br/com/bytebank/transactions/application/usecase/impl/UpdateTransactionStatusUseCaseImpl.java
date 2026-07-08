package br.com.bytebank.transactions.application.usecase.impl;


import br.com.bytebank.transactions.application.usecase.UpdateTransactionUseCase;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.TransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatusUseCaseImpl implements UpdateTransactionUseCase {

    private final TransactionRepositoryDomain transactionRepository;

    @Override
    public void execute(UUID id, TransactionStatus status){
        log.info("Receiving transaction to update status. ID={}", id);
        var transaction = transactionRepository.findById(id).orElseThrow(
                ()-> new TransactionException(id)
        );
        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }

}
