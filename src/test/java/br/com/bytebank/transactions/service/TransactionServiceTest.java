package br.com.bytebank.transactions.service;

import br.com.bytebank.transactions.application.factory.OperationExecutor;
import br.com.bytebank.transactions.application.factory.TransactionFactory;
import br.com.bytebank.transactions.application.usecase.impl.*;
import br.com.bytebank.transactions.application.validator.TransactionValidator;
import br.com.bytebank.transactions.domain.contract.AccountClientContract;
import br.com.bytebank.transactions.domain.contract.IdempotencyContract;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FraudScore;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.repository.PendingTransactionContract;
import br.com.bytebank.transactions.domain.repository.TransactionRepositoryDomain;
import br.com.bytebank.transactions.infrastructure.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.responses.WithdrawResponseDTO;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.*;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.FraudScoreEvent;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionUseCaseTest {

    // ========================
    // DEPOSIT USE CASE
    // ========================
    @Nested
    @DisplayName("DepositUseCaseImpl")
    class DepositUseCaseTests {

        @InjectMocks
        DepositUseCaseImpl depositUseCase;

        @Mock TransactionRepositoryDomain transactionRepository;
        @Mock IdempotencyContract cacheValidator;
        @Mock ApplicationEventPublisher eventPublisher;
        @Mock TransactionFactory transactionFactory;
        @Mock TransactionValidator validator;

        @Test
        @DisplayName("Should create transaction as PENDING and publish event")
        void mustCreatePendingTransactionAndPublishEvent() {
            UUID idempotencyKey = UUID.randomUUID();
            DepositRequestDTO dto = new DepositRequestDTO(UUID.randomUUID(), new BigDecimal("100.00"));
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);

            when(cacheValidator.get(anyString())).thenReturn(null);
            when(transactionFactory.createTransactionEntity(any(), eq(OperationType.DEPOSIT), eq(TransactionStatus.PENDING_CONFIRMATION)))
                    .thenReturn(transaction);

            depositUseCase.execute(idempotencyKey, dto);

            verify(transactionRepository).save(transaction);
            verify(eventPublisher).publishEvent(any(TransactionCreatedDomainEvent.class));
            verify(cacheValidator).toIdempotencyCache(anyString(), any());
        }

        @Test
        @DisplayName("Should return cached response on duplicate idempotency key")
        void mustReturnCachedResponseOnDuplicateKey() {
            UUID idempotencyKey = UUID.randomUUID();
            DepositRequestDTO dto = new DepositRequestDTO(UUID.randomUUID(), new BigDecimal("100.00"));
            DepositResponseDTO cachedResponse = mock(DepositResponseDTO.class);

            when(cacheValidator.get(anyString())).thenReturn("cached");
            when(cacheValidator.fromIdempotencyCache(anyString(), eq(DepositResponseDTO.class)))
                    .thenReturn(cachedResponse);

            DepositResponseDTO result = depositUseCase.execute(idempotencyKey, dto);

            assertThat(result).isEqualTo(cachedResponse);
            verifyNoInteractions(transactionRepository, eventPublisher, transactionFactory);
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when amount is zero")
        void mustThrowExceptionWhenAmountIsZero() {
            DepositRequestDTO dto = new DepositRequestDTO(UUID.randomUUID(), BigDecimal.ZERO);

            doThrow(new InvalidAmountException("Amount must be greater than zero"))
                    .when(validator).amountValidation(BigDecimal.ZERO);

            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> depositUseCase.execute(UUID.randomUUID(), dto))
                    .withMessage("Amount must be greater than zero");

            verifyNoInteractions(transactionRepository, eventPublisher, cacheValidator);
        }
    }

    // ========================
    // WITHDRAW USE CASE
    // ========================
    @Nested
    @DisplayName("WithdrawUseCaseImpl")
    class WithdrawUseCaseTests {

        @InjectMocks
        WithdrawUseCaseImpl withdrawUseCase;
        @Mock TransactionRepositoryDomain transactionRepository;
        @Mock IdempotencyContract cacheValidator;
        @Mock ApplicationEventPublisher eventPublisher;
        @Mock TransactionFactory transactionFactory;
        @Mock TransactionValidator validator;

        @Test
        @DisplayName("Should create transaction as PENDING and publish event")
        void mustCreatePendingTransactionAndPublishEvent() {
            UUID idempotencyKey = UUID.randomUUID();
            WithdrawRequestDTO dto = new WithdrawRequestDTO(UUID.randomUUID(), new BigDecimal("50.00"));
            Transaction transaction = buildTransaction(OperationType.WITHDRAW, TransactionStatus.PENDING_CONFIRMATION);

            when(cacheValidator.get(anyString())).thenReturn(null);
            when(transactionFactory.createTransactionEntity(any(), eq(OperationType.WITHDRAW), eq(TransactionStatus.PENDING_CONFIRMATION)))
                    .thenReturn(transaction);

            withdrawUseCase.execute(idempotencyKey, dto);

            verify(transactionRepository).save(transaction);
            verify(eventPublisher).publishEvent(any(TransactionCreatedDomainEvent.class));
        }

        @Test
        @DisplayName("Should return cached response on duplicate idempotency key")
        void mustReturnCachedResponseOnDuplicateKey() {
            UUID idempotencyKey = UUID.randomUUID();
            WithdrawRequestDTO dto = new WithdrawRequestDTO(UUID.randomUUID(), new BigDecimal("50.00"));
            WithdrawResponseDTO cachedResponse = mock(WithdrawResponseDTO.class);

            when(cacheValidator.get(anyString())).thenReturn("cached");
            when(cacheValidator.fromIdempotencyCache(anyString(), eq(WithdrawResponseDTO.class)))
                    .thenReturn(cachedResponse);

            WithdrawResponseDTO result = withdrawUseCase.execute(idempotencyKey, dto);

            assertThat(result).isEqualTo(cachedResponse);
            verifyNoInteractions(transactionRepository, eventPublisher, transactionFactory);
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when amount is zero")
        void mustThrowExceptionWhenAmountIsZero() {
            WithdrawRequestDTO dto = new WithdrawRequestDTO(UUID.randomUUID(), BigDecimal.ZERO);

            doThrow(new InvalidAmountException("Amount must be greater than zero"))
                    .when(validator).amountValidation(BigDecimal.ZERO);

            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> withdrawUseCase.execute(UUID.randomUUID(), dto))
                    .withMessage("Amount must be greater than zero");

            verifyNoInteractions(transactionRepository, eventPublisher);
        }
    }

    // ========================
    // TRANSFERENCE USE CASE
    // ========================
    @Nested
    @DisplayName("TransferenceUseCaseImpl")
    class TransferenceUseCaseTests {

        @InjectMocks
        TransferenceUseCaseImpl transferenceUseCase;
        @Mock TransactionRepositoryDomain transactionRepository;
        @Mock IdempotencyContract cacheValidator;
        @Mock ApplicationEventPublisher eventPublisher;
        @Mock TransactionFactory transactionFactory;
        @Mock TransactionValidator validator;

        @Test
        @DisplayName("Should create PENDING transfer and publish event")
        void mustCreatePendingTransferAndPublishEvent() {
            UUID idempotencyKey = UUID.randomUUID();
            UUID originId = UUID.randomUUID();
            UUID targetId = UUID.randomUUID();
            TransferenceRequestDTO dto = new TransferenceRequestDTO(originId, targetId, new BigDecimal("200.00"));
            Transaction transaction = buildTransaction(OperationType.TRANSFER, TransactionStatus.PENDING_CONFIRMATION);

            when(cacheValidator.get(anyString())).thenReturn(null);
            when(validator.getAccountForTransaction(originId))
                    .thenReturn(new AccountResponseDTO(originId, UUID.randomUUID(), "1234", new BigDecimal("500.00")));
            when(validator.getAccountForTransaction(targetId))
                    .thenReturn(new AccountResponseDTO(targetId, UUID.randomUUID(), "5678", new BigDecimal("100.00")));
            when(transactionFactory.createTransactionEntity(any(), eq(OperationType.TRANSFER), eq(TransactionStatus.PENDING_CONFIRMATION)))
                    .thenReturn(transaction);

            transferenceUseCase.execute(idempotencyKey, dto);

            verify(transactionRepository).save(transaction);
            verify(eventPublisher).publishEvent(any(TransactionCreatedDomainEvent.class));
        }

        @Test
        @DisplayName("Should throw SameAccountException when accounts are equal")
        void mustThrowWhenSameAccounts() {
            UUID sameId = UUID.randomUUID();
            TransferenceRequestDTO dto = new TransferenceRequestDTO(sameId, sameId, new BigDecimal("100.00"));

            when(cacheValidator.get(anyString())).thenReturn(null);
            doThrow(new SameAccountException("The accounts must be different"))
                    .when(validator).validatingTransference(dto);

            assertThatExceptionOfType(SameAccountException.class)
                    .isThrownBy(() -> transferenceUseCase.execute(UUID.randomUUID(), dto))
                    .withMessage("The accounts must be different");

            verifyNoInteractions(transactionRepository, eventPublisher);
        }

        @Test
        @DisplayName("Should throw AccountNotFoundException when origin account not found")
        void mustThrowWhenOriginAccountNotFound() {
            UUID originId = UUID.randomUUID();
            UUID targetId = UUID.randomUUID();
            TransferenceRequestDTO dto = new TransferenceRequestDTO(originId, targetId, new BigDecimal("100.00"));

            when(cacheValidator.get(anyString())).thenReturn(null);
            when(validator.getAccountForTransaction(originId))
                    .thenThrow(new AccountNotFoundException(originId));

            assertThatExceptionOfType(AccountNotFoundException.class)
                    .isThrownBy(() -> transferenceUseCase.execute(UUID.randomUUID(), dto));

            verify(validator, never()).getAccountForTransaction(targetId);
            verifyNoInteractions(transactionRepository, eventPublisher);
        }
    }

    // ========================
    // FRAUD CALLBACK USE CASE
    // ========================
    @Nested
    @DisplayName("FraudCallBackUseCaseImpl")
    class FraudCallBackUseCaseTests {

        @InjectMocks
        FraudCallBackUSeCaseImpl fraudCallBackUseCase;
        @Mock TransactionRepositoryDomain transactionRepositoryDomain;
        @Mock ApplicationEventPublisher eventPublisher;
        @Mock OperationExecutor executor;

        @Test
        @DisplayName("Should execute deposit when score is LOW and type is DEPOSIT")
        void mustExecuteDepositOnLowScore() {
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);
            FraudScoreEvent event = new FraudScoreEvent(transactionId, FraudScore.LOW);

            when(transactionRepositoryDomain.findById(transactionId)).thenReturn(Optional.of(transaction));

            fraudCallBackUseCase.execute(event);

            verify(executor).executeDeposit(transaction);
        }

        @Test
        @DisplayName("Should execute withdraw when score is LOW and type is WITHDRAW")
        void mustExecuteWithdrawOnLowScore() {
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = buildTransaction(OperationType.WITHDRAW, TransactionStatus.PENDING_CONFIRMATION);
            FraudScoreEvent event = new FraudScoreEvent(transactionId, FraudScore.LOW);

            when(transactionRepositoryDomain.findById(transactionId)).thenReturn(Optional.of(transaction));

            fraudCallBackUseCase.execute(event);

            verify(executor).executeWithdraw(transaction);
        }

        @Test
        @DisplayName("Should set PENDING_CONFIRMATION and publish event when score is MEDIUM")
        void mustSetPendingConfirmationOnMediumScore() {
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);
            FraudScoreEvent event = new FraudScoreEvent(transactionId, FraudScore.MEDIUM);

            when(transactionRepositoryDomain.findById(transactionId)).thenReturn(Optional.of(transaction));

            fraudCallBackUseCase.execute(event);

            verify(transactionRepositoryDomain).save(transaction);
            verify(eventPublisher).publishEvent(any(TransactionCreatedDomainEvent.class));
            verifyNoInteractions(executor);
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING_CONFIRMATION);
        }

        @Test
        @DisplayName("Should block transaction when score is HIGH")
        void mustBlockTransactionOnHighScore() {
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);
            FraudScoreEvent event = new FraudScoreEvent(transactionId, FraudScore.HIGH);

            when(transactionRepositoryDomain.findById(transactionId)).thenReturn(Optional.of(transaction));

            fraudCallBackUseCase.execute(event);

            verify(executor).blockTransaction(transaction);
        }

        @Test
        @DisplayName("Should throw TransactionException when transaction not found")
        void mustThrowWhenTransactionNotFound() {
            UUID transactionId = UUID.randomUUID();
            FraudScoreEvent event = new FraudScoreEvent(transactionId, FraudScore.LOW);

            when(transactionRepositoryDomain.findById(transactionId)).thenReturn(Optional.empty());

            assertThatExceptionOfType(TransactionException.class)
                    .isThrownBy(() -> fraudCallBackUseCase.execute(event));
        }
    }

    // ========================
    // OPERATION EXECUTOR
    // ========================
    @Nested
    @DisplayName("OperationExecutor")
    class OperationExecutorTests {

        @InjectMocks OperationExecutor operationExecutor;
        @Mock TransactionRepositoryDomain transactionRepository;
        @Mock ApplicationEventPublisher eventPublisher;
        @Mock AccountClientContract accountClient;
        @Mock PendingTransactionContract pendingTransactionContract;
        @Mock TransactionFactory factory;

        @Test
        @DisplayName("Should complete deposit and publish event when credit succeeds")
        void mustCompleteDepositOnSuccess() {
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);

            doNothing().when(accountClient).credit(any(), any());

            operationExecutor.executeDeposit(transaction);

            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            verify(transactionRepository).save(transaction);
            verify(eventPublisher).publishEvent(any(TransactionCreatedDomainEvent.class));
        }

        @Test
        @DisplayName("Should mark as PENDING when credit fails")
        void mustMarkAsPendingWhenCreditFails() {
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);
            PendingTransaction pendingTransaction = mock(PendingTransaction.class);

            doThrow(new AccountServiceUnavailableException())
                    .when(accountClient).credit(any(), any());
            when(factory.createPendingTransaction(any(), any())).thenReturn(pendingTransaction);

            operationExecutor.executeDeposit(transaction);

            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            verify(pendingTransactionContract).save(pendingTransaction);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should block transaction and publish event")
        void mustBlockTransactionAndPublishEvent() {
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);

            operationExecutor.blockTransaction(transaction);

            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.BLOCKED);
            verify(transactionRepository).save(transaction);
            verify(eventPublisher).publishEvent(any(TransactionCreatedDomainEvent.class));
        }

        @Test
        @DisplayName("Should complete transfer when debit and credit succeed")
        void mustCompleteTransferOnSuccess() {
            Transaction transaction = buildTransaction(OperationType.TRANSFER, TransactionStatus.PENDING_CONFIRMATION);
            transaction.setOriginAccountId(UUID.randomUUID());
            transaction.setTargetAccountId(UUID.randomUUID());

            doNothing().when(accountClient).debit(any(), any());
            doNothing().when(accountClient).credit(any(), any());

            operationExecutor.executeTransfer(transaction);

            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            verify(transactionRepository).save(transaction);
            verify(eventPublisher).publishEvent(any(TransactionCreatedDomainEvent.class));
        }

        @Test
        @DisplayName("Should mark as PENDING and not attempt credit when debit fails")
        void mustMarkAsPendingAndSkipCreditWhenDebitFails() {
            Transaction transaction = buildTransaction(OperationType.TRANSFER, TransactionStatus.PENDING_CONFIRMATION);
            PendingTransaction pendingTransaction = mock(PendingTransaction.class);

            doThrow(new AccountServiceUnavailableException())
                    .when(accountClient).debit(any(), any());
            when(factory.createPendingTransaction(any(), any())).thenReturn(pendingTransaction);

            operationExecutor.executeTransfer(transaction);

            verify(accountClient, never()).credit(any(), any());
            verify(pendingTransactionContract).save(pendingTransaction);
        }
    }

    // ========================
    // USER CONFIRMATION USE CASE
    // ========================
    @Nested
    @DisplayName("UserConfirmationUseCaseImpl")
    class UserConfirmationUseCaseTests {

        @InjectMocks
        UserConfirmationUseCaseImpl userConfirmationUseCase;
        @Mock AccountClientContract accountClient;
        @Mock OperationExecutor executor;
        @Mock PendingTransactionContract pendingTransactionContract;

        @Test
        @DisplayName("Should execute deposit when user confirms with 'sim' and type is DEPOSIT")
        void mustExecuteDepositOnConfirmation() {
            UUID customerId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);
            PendingTransaction pending = buildPendingTransaction(transaction);

            when(accountClient.findAccountByCustomerId(customerId))
                    .thenReturn(new AccountResponseDTO(accountId, customerId, "1234", BigDecimal.TEN));
            when(pendingTransactionContract.findByOriginAccountIdAndTransactionStatus(accountId, TransactionStatus.PENDING_CONFIRMATION))
                    .thenReturn(Optional.of(pending));

            userConfirmationUseCase.execute(customerId, "sim");

            verify(executor).executeDeposit(transaction);
        }

        @Test
        @DisplayName("Should block transaction when user answers 'não'")
        void mustBlockTransactionOnDenial() {
            UUID customerId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);
            PendingTransaction pending = buildPendingTransaction(transaction);

            when(accountClient.findAccountByCustomerId(customerId))
                    .thenReturn(new AccountResponseDTO(accountId, customerId, "1234", BigDecimal.TEN));
            when(pendingTransactionContract.findByOriginAccountIdAndTransactionStatus(accountId, TransactionStatus.PENDING_CONFIRMATION))
                    .thenReturn(Optional.of(pending));

            userConfirmationUseCase.execute(customerId, "não");

            verify(executor).blockTransaction(transaction);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException on invalid answer")
        void mustThrowOnInvalidAnswer() {
            UUID customerId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Transaction transaction = buildTransaction(OperationType.DEPOSIT, TransactionStatus.PENDING_CONFIRMATION);
            PendingTransaction pending = buildPendingTransaction(transaction);

            when(accountClient.findAccountByCustomerId(customerId))
                    .thenReturn(new AccountResponseDTO(accountId, customerId, "1234", BigDecimal.TEN));
            when(pendingTransactionContract.findByOriginAccountIdAndTransactionStatus(accountId, TransactionStatus.PENDING_CONFIRMATION))
                    .thenReturn(Optional.of(pending));

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> userConfirmationUseCase.execute(customerId, "talvez"));
        }
    }

    // ========================
    // HELPERS — instâncias reais, não mocks
    // ========================
    private Transaction buildTransaction(OperationType type, TransactionStatus status) {
        Transaction t = new Transaction();
        t.setType(type);
        t.setStatus(status);
        t.setAmount(new BigDecimal("100.00"));
        t.setOriginAccountId(UUID.randomUUID());
        return t;
    }

    private PendingTransaction buildPendingTransaction(Transaction source) {
        PendingTransaction pt = new PendingTransaction();
        pt.setSourceTransaction(source);
        pt.setOriginAccountId(source.getOriginAccountId());
        pt.setTransactionStatus(source.getStatus());
        return pt;
    }
}