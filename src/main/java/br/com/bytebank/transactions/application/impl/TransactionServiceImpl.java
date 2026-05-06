package br.com.bytebank.transactions.application.impl;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.application.services.TransactionService;
import br.com.bytebank.transactions.domain.entities.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.exceptions.InvalidAmountException;
import br.com.bytebank.transactions.infrastructure.feignclients.AccountClient;
import br.com.bytebank.transactions.infrastructure.repositories.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
//
//    @Transactional
//    public void transference(UUID originAccountId, UUID destinationAccountId, BigDecimal amount){
//        Account originAccount = getAccount(originAccountId, "Origin Account not Found");
//        Account destinationAccount = getAccount(destinationAccountId, "Destination Account not Found");
//
//        if (originAccount == destinationAccount){
//            log.warn("User informed identical accounts. originAccountId={}, destinationAccountId={} ", originAccountId, destinationAccountId);
//            throw new SameAccountException("The accounts must be different");
//
//        }
//
//        amountValidation(amount);
//        balanceValidation(originAccount, amount);
//
//        originAccount.debit(amount);
//        destinationAccount.credit(amount);
//
//        originAccount.addTransactions(new Transaction(OperationType.TRANSFER, amount));
//        destinationAccount.addTransactions(new Transaction(OperationType.TRANSFER, amount, "Received"));
//        log.info("Transference succeeded. originAccountId={}, destinationAccountId={}, value={}", originAccountId, destinationAccountId, amount);
//        accountRepository.save(originAccount);
//        accountRepository.save(destinationAccount);
//
//    }
//
//
//
    protected static void amountValidation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Invalid value entered. value={}", amount);
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }
//
//
//
//    @Transactional
//    public List<TransactionResponseDTO> generateBankStatement(UUID id)  {
//        Account account = accountRepository.findById(id)
//                .orElseThrow(()-> new AccountNotFoundException("Account not found"));
//
//        return account.getTransactions()
//                .stream()
//                .map(t-> new TransactionResponseDTO(t.getId(), t.getType(), t.getAmount(), t.getDescription(), t.getDateTime()))
//                .toList();
//    }

}
