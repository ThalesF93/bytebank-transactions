package br.com.bytebank.transactions.application.usecase.impl;


import br.com.bytebank.transactions.application.usecase.UpdateTransactionUseCase;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.TransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateTransactionStatusUseCaseImpl implements UpdateTransactionUseCase {

    private final TransactionRepositoryDomain transactionRepository;

    @Override
    public void execute(UUID id, TransactionStatus status){
        var transaction = transactionRepository.findById(id).orElseThrow(
                ()-> new TransactionException(id)
        );

        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }

}
