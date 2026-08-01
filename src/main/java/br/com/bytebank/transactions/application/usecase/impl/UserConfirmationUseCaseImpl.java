package br.com.bytebank.transactions.application.usecase.impl;

import br.com.bytebank.transactions.application.factory.OperationExecutor;
import br.com.bytebank.transactions.application.usecase.UserConfirmationUseCase;
import br.com.bytebank.transactions.domain.contract.AccountClientContract;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.PendingTransactionContract;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.OperationTypeNoneExistingException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.ResourceNotFoundException;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.TransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserConfirmationUseCaseImpl implements UserConfirmationUseCase {

    private final AccountClientContract accountClient;
    private final OperationExecutor executor;
    private final TransactionRepositoryDomain transactionRepository;

    @Override
    public void execute(UUID uuid, String answer) {
        log.info("Starting operation after received answer from User");
        var account = accountClient.findAccountByCustomerId(uuid);

        var transaction = transactionRepository
                .findByOriginAccountIdAndStatus(account.accountId(), TransactionStatus.PENDING_CONFIRMATION)
                .orElseThrow(() -> new TransactionException(account.accountId()));

        String answerLowerCase = answer.toLowerCase();

        switch (answerLowerCase) {
            case "nao", "não" -> executor.blockTransaction(transaction);
            case "sim" -> {
                switch (transaction.getType()) {
                    case DEPOSIT -> executor.executeDeposit(transaction);
                    case WITHDRAW -> executor.executeWithdraw(transaction);
                    case TRANSFER -> executor.executeTransfer(transaction);
                    default -> throw new OperationTypeNoneExistingException(transaction.getType());
                }
            }
            default -> throw new ResourceNotFoundException("Invalid Answer to proceed any operations");
        }
    }
}