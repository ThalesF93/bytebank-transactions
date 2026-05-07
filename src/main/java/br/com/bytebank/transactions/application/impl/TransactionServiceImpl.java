package br.com.bytebank.transactions.application.impl;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.application.service.TransactionService;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.exception.InvalidAmountException;
import br.com.bytebank.transactions.domain.exception.SameAccountException;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.repositories.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;


    @Transactional
    public TransactionResponseDTO withdraw(WithdrawRequestDTO requestDTO){

        amountValidation(requestDTO.amount());


        Transaction transaction = null;
        try {
            accountClient.debit(new WithdrawRequestDTO(requestDTO.accountId(), requestDTO.amount()));

            transaction = new Transaction();
            transaction.setOriginAccountId(requestDTO.accountId());
            transaction.setType(OperationType.WITHDRAW);
            transaction.setAmount(requestDTO.amount());
            transaction.setDateTime(LocalDateTime.now());
            transaction.setStatus(TransactionStatus.COMPLETED);

            transactionRepository.save(transaction);
        } catch (FeignException e) {
            throw new RuntimeException(e);
        }
        log.info("Withdraw succeeded. accountId={}, value={}", requestDTO.accountId(), requestDTO.amount());

        return new TransactionResponseDTO(
                transaction.getId(), transaction.getType(), transaction.getAmount(), transaction.getDescription(), transaction.getDateTime()
        );
    }

    @Transactional
    public TransactionResponseDTO deposit(DepositRequestDTO requestDTO){

        amountValidation(requestDTO.amount());

        accountClient.credit(new DepositRequestDTO(requestDTO.accountId(), requestDTO.amount()));

        Transaction transaction = new Transaction();
        transaction.setOriginAccountId(requestDTO.accountId());
        transaction.setType(OperationType.DEPOSIT);
        transaction.setAmount(requestDTO.amount());
        transaction.setDateTime(LocalDateTime.now());

        log.info("Deposit succeeded. accountId={}, value={}", requestDTO.accountId(), requestDTO.amount());
        transactionRepository.save(transaction);

        return new TransactionResponseDTO(
                transaction.getId(), transaction.getType(), transaction.getAmount(), transaction.getDescription(), transaction.getDateTime()
        );
    }

    @Transactional
    @Override
    public TransactionResponseDTO transference(UUID originAccountId, UUID destinationAccountId, BigDecimal amount){

        amountValidation(amount);

        var originAccount = accountClient.findAccount(originAccountId);
        var destinationAccount = accountClient.findAccount(destinationAccountId);

        if (originAccountId.equals(destinationAccountId)){
            log.warn("User informed identical accounts. originAccountId={}, destinationAccountId={} ", originAccountId, destinationAccountId);
            throw new SameAccountException("The accounts must be different");

        }
        Transaction transaction = new Transaction();
        transaction.setOriginAccountId(originAccountId);
        transaction.setTargetAccountId(destinationAccountId);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.PROCESSING);

        transactionRepository.save(transaction);

        try{
            accountClient.debit(new WithdrawRequestDTO(originAccount.getBody().accountId(), amount));
            accountClient.credit(new DepositRequestDTO(destinationAccount.getBody().accountId(),amount ));

            transaction.setStatus(TransactionStatus.COMPLETED);
            log.info("Transference succeeded. originAccountId={}, destinationAccountId={}, value={}", originAccountId, destinationAccountId, amount);
            transactionRepository.save(transaction);

            return TransactionResponseDTO.transferenceCompletedResponse(transaction);
        }catch (RuntimeException e){

            return TransactionResponseDTO.transferencePendingResponse(transaction);

        }

    }



    protected static void amountValidation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Invalid value entered. value={}", amount);
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }



    @Transactional
    @Override
    public List<TransactionResponseDTO> generateBankStatement(UUID id)  {
        Account account = accountRepository.findById(id)
                .orElseThrow(()-> new AccountNotFoundException("Account not found"));

        return account.getTransactions()
                .stream()
                .map(t-> new TransactionResponseDTO(t.getId(), t.getType(), t.getAmount(), t.getDescription(), t.getDateTime()))
                .toList();
    }

}
