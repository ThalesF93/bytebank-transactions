package br.com.bytebank.transactions.service;

import br.com.bytebank.transactions.TestBuilders;
import br.com.bytebank.transactions.application.usecase.deposit_usecase.DepositUseCase;
import br.com.bytebank.transactions.application.usecase.transference_usecase.TransferenceUseCase;
import br.com.bytebank.transactions.application.usecase.withdraw_usecase.WithdrawUseCase;
import br.com.bytebank.transactions.infrastructure.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.WithdrawResponseDTO;
import br.com.bytebank.transactions.application.service.TransactionServiceImpl;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.*;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.messaging.rabbitmq.TransactionEventPublisher;
import br.com.bytebank.transactions.infrastructure.database.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.database.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    ValueOperations<String, Object> valueOperations;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    DepositUseCase depositUseCase;

    @Mock
    WithdrawUseCase withdrawUseCase;

    @Mock
    TransferenceUseCase transferenceUseCase;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Should successfully withdraw")
    void mustWithdraw() throws Exception {

        UUID idempotencyKey = UUID.randomUUID();
        WithdrawRequestDTO dto = TestBuilders.withdrawRequestDTO();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        doNothing().when(accountClient)
                .debit(any(WithdrawRequestDTO.class));

        WithdrawResponseDTO result =
                withdrawUseCase.execute(idempotencyKey, dto);

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountClient).debit(any(WithdrawRequestDTO.class));

        assertThat(result.amount()).isEqualTo(dto.amount());
    }

    @Test
    @DisplayName("Should throw Feign Exception and Save the Withdraw as pending")
    void mustThrowExceptionAndSaveWithdrawAsPending() throws Exception {

        UUID idempotencyKey = UUID.randomUUID();
        WithdrawRequestDTO dto = TestBuilders.withdrawRequestDTO();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(accountClient.debit(any(WithdrawRequestDTO.class)))
                .thenThrow(FeignException.class);

        when(pendingRepository.save(any(PendingTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WithdrawResponseDTO result =
                withdrawUseCase.execute(idempotencyKey, dto);

        verify(transactionRepository, times(1))
                .save(any(Transaction.class));

        verify(accountClient, times(1))
                .debit(any(WithdrawRequestDTO.class));

        verify(pendingRepository, times(1))
                .save(any(PendingTransaction.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should Throw Invalid Amount Exception on Withdraw")
    void mustThrowExceptionInWithdrawBalanceValidation() {

        UUID idempotencyKey = UUID.randomUUID();

        WithdrawRequestDTO dto =
                new WithdrawRequestDTO(UUID.randomUUID(), BigDecimal.ZERO);

        assertThatExceptionOfType(InvalidAmountException.class)
                .isThrownBy(() -> withdrawUseCase.execute(idempotencyKey, dto))
                .withMessage("Amount must be greater than zero");

        verify(transactionRepository, never()).save(any());
        verify(accountClient, never()).debit(any());
        verify(pendingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deposit successfully")
    void mustDeposit() throws JsonProcessingException {

        DepositRequestDTO dto = TestBuilders.depositRequestDTO();
        UUID idempotencyKey = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        doNothing().when(valueOperations)
                .set(anyString(), anyString(), any());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(accountClient).credit(any());

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"amount\":10}");

        DepositResponseDTO result = depositUseCase.execute(idempotencyKey, dto);

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountClient, times(1)).credit(any());

        assertThat(result.amount())
                .isEqualByComparingTo(dto.amount());
    }

    @Test
    @DisplayName("Should save deposit as pending when account client fails")
    void mustThrowExceptionAndSaveDepositAsPending() throws JsonProcessingException {
        DepositRequestDTO dto = TestBuilders.depositRequestDTO();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pendingRepository.save(any(PendingTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(Mockito.mock(FeignException.class))
                .when(accountClient).credit(dto);

        DepositResponseDTO result = depositUseCase.execute(UUID.randomUUID(), dto);

        verify(transactionRepository).save(any(Transaction.class));
        verify(pendingRepository).save(any(PendingTransaction.class));
    }

    @Test
    @DisplayName("Should throw Invalid Amount Exception on Deposit")
    void mustThrowExceptionInDepositBalanceValidation() {
        DepositRequestDTO dto = new DepositRequestDTO(UUID.randomUUID(), new BigDecimal("0"));

        assertThatExceptionOfType(InvalidAmountException.class)
                .isThrownBy(() -> depositUseCase.execute(UUID.randomUUID(), dto))
                .withMessage("Amount must be greater than zero");

        verifyNoInteractions(transactionRepository, pendingRepository, accountClient);
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
                .withMessage(String.format("Transaction with id %s not found", id));

        verify(transactionRepository).findById(id);
    }

    @Test
    @DisplayName("Should complete transference and send event")
    void mustCompleteTransference() throws Exception {

        UUID idempotencyKey = UUID.randomUUID();

        UUID idOriginAccount = UUID.randomUUID();
        UUID idTargetAccount = UUID.randomUUID();

        BigDecimal value = new BigDecimal("100.00");

        TransferenceRequestDTO requestDTO =
                new TransferenceRequestDTO(idOriginAccount, idTargetAccount, value);

        AccountResponseDTO originAccount =
                new AccountResponseDTO(idOriginAccount, UUID.randomUUID(), "4567", new BigDecimal("50"));

        AccountResponseDTO targetAccount =
                new AccountResponseDTO(idTargetAccount, UUID.randomUUID(), "4567", new BigDecimal("50"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(accountClient.findAccount(idOriginAccount)).thenReturn(originAccount);
        when(accountClient.findAccount(idTargetAccount)).thenReturn(targetAccount);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result =
                transferenceUseCase.execute(idempotencyKey, requestDTO);

        verify(transactionRepository, times(2))
                .save(any(Transaction.class));

        verify(accountClient).debit(any(WithdrawRequestDTO.class));
        verify(accountClient).credit(any(DepositRequestDTO.class));

        verify(eventPublisher).publishTransferenceCompleted(any(Transaction.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should throw Same account Exception when passing identical accounts in transference")
    void mustThrowExceptionWhenSameAccounts() {

        UUID idempotencyKey = UUID.randomUUID();
        UUID idOriginAccount = UUID.randomUUID();

        TransferenceRequestDTO requestDTO =
                new TransferenceRequestDTO(idOriginAccount, idOriginAccount, new BigDecimal("10"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThatExceptionOfType(SameAccountException.class)
                .isThrownBy(() ->
                        transferenceUseCase.execute(idempotencyKey, requestDTO))
                .withMessage("The accounts must be different");

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(accountClient);
        verifyNoInteractions(pendingRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should throw Account Not found exception when origin account not found")
    void mustThrowExceptionWhenOriginAccountNotFound() throws Exception {

        UUID idempotencyKey = UUID.randomUUID();

        UUID idOriginAccount = UUID.randomUUID();
        UUID idTargetAccount = UUID.randomUUID();

        TransferenceRequestDTO requestDTO =
                new TransferenceRequestDTO(idOriginAccount, idTargetAccount, new BigDecimal("100.00"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(accountClient.findAccount(idOriginAccount))
                .thenThrow(new AccountNotFoundException(idOriginAccount));

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() ->
                        transferenceUseCase.execute(idempotencyKey, requestDTO))
                .withMessage(String.format("Account with id %s not found", idOriginAccount));

        verify(accountClient).findAccount(idOriginAccount);
        verify(accountClient, never()).findAccount(idTargetAccount);

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(pendingRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when destination account not found")
    void mustThrowExceptionWhenDestinationAccountNotFound() throws Exception {

        UUID idempotencyKey = UUID.randomUUID();

        UUID idOriginAccount = UUID.randomUUID();
        UUID idTargetAccount = UUID.randomUUID();

        TransferenceRequestDTO requestDTO =
                new TransferenceRequestDTO(idOriginAccount, idTargetAccount, new BigDecimal("100.00"));

        AccountResponseDTO originAccount =
                new AccountResponseDTO(idOriginAccount, UUID.randomUUID(), "4567", new BigDecimal("500.00"));

        // 🔥 FIX PRINCIPAL: Redis precisa existir no fluxo SEMPRE
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(accountClient.findAccount(idOriginAccount))
                .thenReturn(originAccount);

        when(accountClient.findAccount(idTargetAccount))
                .thenThrow(new AccountNotFoundException(idTargetAccount));

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() ->
                        transferenceUseCase.execute(idempotencyKey, requestDTO));

        verify(accountClient).findAccount(idOriginAccount);
        verify(accountClient).findAccount(idTargetAccount);

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(pendingRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when origin account has not enough balance")
    void mustThrowExceptionWhenInsufficientBalance() throws Exception {

        UUID idempotencyKey = UUID.randomUUID();

        UUID idOriginAccount = UUID.randomUUID();
        UUID idTargetAccount = UUID.randomUUID();

        TransferenceRequestDTO requestDTO =
                new TransferenceRequestDTO(idOriginAccount, idTargetAccount, new BigDecimal("100.00"));

        AccountResponseDTO originAccount =
                new AccountResponseDTO(idOriginAccount, UUID.randomUUID(), "4567", new BigDecimal("50.00"));

        AccountResponseDTO destinationAccount =
                new AccountResponseDTO(idTargetAccount, UUID.randomUUID(), "4567", new BigDecimal("10.00"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(accountClient.findAccount(idOriginAccount)).thenReturn(originAccount);
        when(accountClient.findAccount(idTargetAccount)).thenReturn(destinationAccount);

        doThrow(new InsufficientBalanceException("Insufficient balance"))
                .when(accountClient).debit(any(WithdrawRequestDTO.class));

        assertThatExceptionOfType(InsufficientBalanceException.class)
                .isThrownBy(() ->
                        transferenceUseCase.execute(idempotencyKey, requestDTO))
                .withMessage("Insufficient balance");

        verify(accountClient).findAccount(idOriginAccount);
        verify(accountClient).findAccount(idTargetAccount);

        verify(accountClient).debit(any(WithdrawRequestDTO.class));

        verify(accountClient, never()).credit(any(DepositRequestDTO.class));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should save Pending Transaction when Account client is unavailable")
    void mustSavePendingTransaction() throws Exception {

        UUID idempotencyKey = UUID.randomUUID();

        UUID idOriginAccount = UUID.randomUUID();
        UUID idTargetAccount = UUID.randomUUID();

        TransferenceRequestDTO requestDTO =
                new TransferenceRequestDTO(idOriginAccount, idTargetAccount, new BigDecimal("100.00"));

        AccountResponseDTO originAccount =
                new AccountResponseDTO(idOriginAccount, UUID.randomUUID(), "4567", new BigDecimal("50.00"));

        AccountResponseDTO destinationAccount =
                new AccountResponseDTO(idTargetAccount, UUID.randomUUID(), "4567", new BigDecimal("10.00"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(accountClient.findAccount(idOriginAccount)).thenReturn(originAccount);
        when(accountClient.findAccount(idTargetAccount)).thenReturn(destinationAccount);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // 🔥 IMPORTANTE: lançar exception compatível com service
        doThrow(new ServiceUnavailableException("Account service unavailable"))
                .when(accountClient).debit(any(WithdrawRequestDTO.class));

        TransactionResponseDTO response =
                transferenceUseCase.execute(idempotencyKey, requestDTO);

        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);

        verify(pendingRepository, times(1))
                .save(any(PendingTransaction.class));

        verify(accountClient, never()).credit(any());

        verifyNoInteractions(eventPublisher);
    }

}
