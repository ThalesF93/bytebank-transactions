package br.com.bytebank.transactions.service;

import br.com.bytebank.transactions.TestBuilders;
import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.application.impl.TransactionServiceImpl;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.exception.InvalidAmountException;
import br.com.bytebank.transactions.domain.exception.TransactionException;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.messaging.TransactionEventPublisher;
import br.com.bytebank.transactions.infrastructure.repositories.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.repositories.TransactionRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    TransactionServiceImpl transactionService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountClient accountClient;

    @Mock
    TransactionEventPublisher eventPublisher;

    @Mock
    PendingTransactionRepository pendingRepository;

    @Test
    @DisplayName("Should successfully withdraw")
    void mustWithdraw(){
        WithdrawRequestDTO dto = TestBuilders.withdrawRequestDTO();
        Transaction transaction = TestBuilders.createWithdrawTransaction(dto);

        var result = transactionService.withdraw(dto);

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountClient).debit(dto);

        assertThat(result.amount()).isEqualTo(dto.amount());
    }

    @Test
    @DisplayName("Should throw Feign Exception and Save the Withdraw as pending")
    void mustThrowExceptionAndSaveWithdrawAsPending(){
        WithdrawRequestDTO dto = TestBuilders.withdrawRequestDTO();

        when(accountClient.debit(dto)).thenThrow(FeignException.class);
        when(pendingRepository.save(any(PendingTransaction.class))).thenReturn(any(PendingTransaction.class));

        var result = transactionService.withdraw(dto);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(pendingRepository, times(1)).save(any(PendingTransaction.class));

    }

    @Test
    @DisplayName("Should Throw Invalid Amount Exception on Withdraw")
    void mustThrowExceptionInWithdrawBalanceValidation(){
        WithdrawRequestDTO dto = new WithdrawRequestDTO(UUID.randomUUID(), new BigDecimal("0"));

        assertThatExceptionOfType(InvalidAmountException.class)
                .isThrownBy(()-> transactionService.withdraw(dto))
                .withMessage("Amount must be greater than zero");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully deposit")
    void mustDeposit(){
       DepositRequestDTO dto = TestBuilders.depositRequestDTO();
        Transaction transaction = TestBuilders.createDepositTransaction(dto);

        var result = transactionService.deposit(dto);

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountClient).credit(dto);

        assertThat(result.amount()).isEqualTo(dto.amount());
    }

    @Test
    @DisplayName("Should throw Feign Exception and Save the Deposit as pending")
    void mustThrowExceptionAndSaveDepositAsPending(){
        DepositRequestDTO dto = TestBuilders.depositRequestDTO();

        when(accountClient.credit(dto)).thenThrow(FeignException.class);
        when(pendingRepository.save(any(PendingTransaction.class))).thenReturn(any(PendingTransaction.class));

        var result = transactionService.deposit(dto);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(pendingRepository, times(1)).save(any(PendingTransaction.class));

    }

    @Test
    @DisplayName("Should Throw Invalid Amount Exception on Deposit")
    void mustThrowExceptionInDepositBalanceValidation(){
        DepositRequestDTO dto = new DepositRequestDTO(UUID.randomUUID(), new BigDecimal("0"));

        assertThatExceptionOfType(InvalidAmountException.class)
                .isThrownBy(()-> transactionService.deposit(dto))
                .withMessage("Amount must be greater than zero");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should generate bank statement ordered by repository result")
    void shouldGenerateBankStatement() {
        UUID accountId = UUID.randomUUID();

        Transaction transaction1 = TestBuilders.createDepositTransaction(
                new DepositRequestDTO(UUID.randomUUID(), new BigDecimal("100.00")));

        Transaction transaction2 = TestBuilders.createDepositTransaction(
                new DepositRequestDTO(UUID.randomUUID(), new BigDecimal("500.00")));

        when(transactionRepository
                .findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(accountId, accountId))
                .thenReturn(List.of(transaction1, transaction2));

        var result = transactionService.generateBankStatement(accountId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(BankStatementResponseDTO.generateStatement(transaction1));
        assertThat(result.get(1)).isEqualTo(BankStatementResponseDTO.generateStatement(transaction2));

        verify(transactionRepository)
                .findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(accountId, accountId);
    }

    @Test
    @DisplayName("Should return empty bank statement when no transactions are found")
    void shouldReturnEmptyBankStatement() {
        UUID accountId = UUID.randomUUID();

        when(transactionRepository
                .findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(accountId, accountId))
                .thenReturn(Collections.emptyList());

        var result = transactionService.generateBankStatement(accountId);

        assertThat(result).isEmpty();

        verify(transactionRepository)
                .findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(accountId, accountId);
    }

    @Test
    @DisplayName("Should return transaction by id")
    void shouldReturnTransactionById() {
        UUID id = UUID.randomUUID();

        Transaction transaction = TestBuilders.createDepositTransaction(
                new DepositRequestDTO(UUID.randomUUID(), new BigDecimal("100.00")));

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        TransactionResponseDTO result = transactionService.getTransactionById(id);

        assertThat(result.type()).isEqualTo(transaction.getType());
        assertThat(result.status()).isEqualTo(transaction.getStatus());
        assertThat(result.amount()).isEqualByComparingTo(transaction.getAmount());
        assertThat(result).isNotNull();

        verify(transactionRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw TransactionException when transaction does not exist")
    void shouldThrowWhenTransactionNotFound() {
        UUID id = UUID.randomUUID();

        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TransactionException.class)
                .isThrownBy(() -> transactionService.getTransactionById(id))
                .withMessage("Transaction not found. ID= " + id);

        verify(transactionRepository).findById(id);
    }
}
