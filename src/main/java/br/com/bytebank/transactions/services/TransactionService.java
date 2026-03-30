package br.com.bytebank.transactions.services;

import br.com.bytebank.transactions.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.entities.Transaction;
import br.com.bytebank.transactions.enums.OperationType;
import br.com.coderbank.operacoes_bancarias.entities.Account;
import br.com.coderbank.operacoes_bancarias.entities.Transaction;
import br.com.coderbank.operacoes_bancarias.enums.OperationType;
import br.com.coderbank.operacoes_bancarias.exceptions.AccountNotFoundException;
import br.com.coderbank.operacoes_bancarias.exceptions.InsufficientBalanceException;
import br.com.coderbank.operacoes_bancarias.exceptions.InvalidAmountException;
import br.com.coderbank.operacoes_bancarias.exceptions.SameAccountException;
import br.com.coderbank.operacoes_bancarias.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;


    @Transactional
    public void withdraw(UUID id, BigDecimal amount){
        Account account = getAccount(id, "Account Not found");

        amountValidation(amount);
        balanceValidation(account,amount);

        account.debit(amount);
        account.addTransactions(new Transaction(OperationType.WITHDRAW, amount));
        log.info("Withdraw succeeded. accountId={}, value={}", account.getId(), amount);
        accountRepository.save(account);
    }

    @Transactional
    public void deposit(UUID id, BigDecimal amount){
        Account account = getAccount(id, "Account Not found");

        amountValidation(amount);

        account.credit(amount);
        account.addTransactions(new Transaction(OperationType.DEPOSIT, amount));
        log.info("Deposit succeeded. accountId={}, value={}", account.getId(), amount);
        accountRepository.save(account);
    }

    @Transactional
    public void transference(UUID originAccountId, UUID destinationAccountId, BigDecimal amount){
        Account originAccount = getAccount(originAccountId, "Origin Account not Found");
        Account destinationAccount = getAccount(destinationAccountId, "Destination Account not Found");

        if (originAccount == destinationAccount){
            log.warn("User informed identical accounts. originAccountId={}, destinationAccountId={} ", originAccountId, destinationAccountId);
            throw new SameAccountException("The accounts must be different");

        }

        amountValidation(amount);
        balanceValidation(originAccount, amount);

        originAccount.debit(amount);
        destinationAccount.credit(amount);

        originAccount.addTransactions(new Transaction(OperationType.TRANSFER, amount));
        destinationAccount.addTransactions(new Transaction(OperationType.TRANSFER, amount, "Received"));
        log.info("Transference succeeded. originAccountId={}, destinationAccountId={}, value={}", originAccountId, destinationAccountId, amount);
        accountRepository.save(originAccount);
        accountRepository.save(destinationAccount);

    }

    private Account getAccount(UUID accountId, String messageError) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(messageError));
    }

    protected static void amountValidation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Invalid value entered. value={}", amount);
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    protected void balanceValidation(Account account, BigDecimal amount) {
        if (isBalanceInsufficient(account, amount)){
            log.warn("Withdraw must not be more than balance, value={}, balance={}", amount, account.getBalance());
            throw new InsufficientBalanceException("Unauthorized operation! Withdraw must not be more than balance");
        }
    }

    private static boolean isBalanceInsufficient(Account account, BigDecimal amount) {
        return account.getBalance().compareTo(amount) < 0;
    }

    @Transactional
    public List<TransactionResponseDTO> generateBankStatement(UUID id)  {
        Account account = accountRepository.findById(id)
                .orElseThrow(()-> new AccountNotFoundException("Account not found"));

        return account.getTransactions()
                .stream()
                .map(t-> new TransactionResponseDTO(t.getId(), t.getType(), t.getAmount(), t.getDescription(), t.getDateTime()))
                .toList();
    }

}
