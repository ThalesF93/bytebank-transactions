package br.com.bytebank.transactions.application.impl;

import br.com.bytebank.transactions.api.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.api.dtos.requests.AccountOperationRequest;
import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.WithdrawResponseDTO;
import br.com.bytebank.transactions.application.service.TransactionService;
import br.com.bytebank.transactions.domain.entity.PendingTransaction;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import br.com.bytebank.transactions.domain.exception.customized_exceptions.*;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.messaging.TransactionEventPublisher;
import br.com.bytebank.transactions.infrastructure.repositories.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.repositories.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final PendingTransactionRepository pendingTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final TransactionEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public WithdrawResponseDTO withdraw(UUID idempotencyKey, WithdrawRequestDTO requestDTO) throws JsonProcessingException {
        amountValidation(requestDTO.amount());

        String cacheKey = "idempotency:deposit:" + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Duplicate withdraw detected. idempotencyKey={}", idempotencyKey);
            return objectMapper.readValue(cached.toString(), WithdrawResponseDTO.class);
        }

        Transaction transaction = createTransactionEntity(requestDTO, OperationType.WITHDRAW, TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        try {
            accountClient.debit(new WithdrawRequestDTO(requestDTO.accountId(), requestDTO.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
            log.info("Withdraw succeeded. accountId={}, value={}", requestDTO.accountId(), requestDTO.amount());
            transactionRepository.save(transaction);

        } catch (FeignException e) {

            PendingTransaction pendingTransaction = createPendingTransaction(transaction, FailureReason.DEBIT_FAILED);
            pendingTransactionRepository.save(pendingTransaction);
        }
        return WithdrawResponseDTO.response(transaction);
    }


    @Override
    public DepositResponseDTO deposit(UUID idempotencyKey, DepositRequestDTO requestDTO) throws JsonProcessingException {
        amountValidation(requestDTO.amount());

        String cacheKey = "idempotency:deposit:" + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Duplicate deposit detected. idempotencyKey={}", idempotencyKey);
            return objectMapper.readValue(cached.toString(), DepositResponseDTO.class);
        }

        Transaction transaction = createTransactionEntity(requestDTO, OperationType.DEPOSIT, TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        try {
            accountClient.credit(new DepositRequestDTO(requestDTO.accountId(), requestDTO.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
            log.info("Deposit succeeded. accountId={}, value={}", requestDTO.accountId(), requestDTO.amount());
            transactionRepository.save(transaction);

        } catch (FeignException e) {
            transaction.setStatus(TransactionStatus.PENDING);
            PendingTransaction pendingTransaction = createPendingTransaction(transaction, FailureReason.CREDIT_FAILED);
            pendingTransactionRepository.save(pendingTransaction);
        }

        var response = DepositResponseDTO.response(transaction);
        redisTemplate.opsForValue().set(
                cacheKey,
                objectMapper.writeValueAsString(response),
                Duration.ofHours(24)
        );

        return response;
    }

    @Override
    public TransactionResponseDTO transference(UUID idempotencyKey, TransferenceRequestDTO dto) throws JsonProcessingException {

        String cacheKey = "idempotency:deposit:" + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Duplicate transference detected. idempotencyKey={}", idempotencyKey);
            return objectMapper.readValue(cached.toString(), TransactionResponseDTO.class);
        }

        validatingTransference(dto);

        AccountResponseDTO originAccount;
        AccountResponseDTO destinationAccount;

        try {
            originAccount = accountClient.findAccount(dto.originAccountId());
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException(dto.originAccountId());
        }catch (FeignException e){
            throw new AccountServiceUnavailableException();
        }

        try {
            destinationAccount = accountClient.findAccount(dto.destinationAccountId());
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException (dto.destinationAccountId());
        }catch (FeignException e){
            throw new AccountServiceUnavailableException();
        }

        Transaction transaction = createTransactionEntity(dto, OperationType.TRANSFER, TransactionStatus.PROCESSING);
        transaction.setTargetAccountId(dto.destinationAccountId());
        transactionRepository.save(transaction);

       return executeTransfer(transaction, dto, originAccount, destinationAccount);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankStatementResponseDTO> generateBankStatement(UUID accountId)  {
        var transactions = transactionRepository.findByOriginAccountIdOrTargetAccountIdOrderByDateTimeDesc(accountId, accountId);
        return transactions
                .stream()
                .map(BankStatementResponseDTO::generateStatement)
                .toList();
    }

    @Override
    public TransactionResponseDTO getTransactionById(UUID id) {
        var transaction = transactionRepository.findById(id).orElseThrow(
                ()-> new TransactionException(id)
        );
        return new TransactionResponseDTO(id, transaction.getTargetAccountId(), transaction.getType(), transaction.getStatus(), transaction.getAmount(), null);
    }

    @NonNull
    private static Transaction createTransactionEntity(AccountOperationRequest requestDTO, OperationType type, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setOriginAccountId(requestDTO.accountId());;
        transaction.setType(type);
        transaction.setAmount(requestDTO.amount());
        transaction.setDateTime(LocalDateTime.now());
        transaction.setStatus(status);
        return transaction;
    }

    private static PendingTransaction createPendingTransaction(Transaction transaction, FailureReason failureReason) {
        return PendingTransaction.builder()
                .sourceTransaction(transaction)
                .originAccountId(transaction.getOriginAccountId())
                .destinationAccountId(transaction.getTargetAccountId())
                .amount(transaction.getAmount())
                .dateTime(transaction.getDateTime())
                .processed(false)
                .operationType(transaction.getType())
                .attempts(1)
                .transactionStatus(TransactionStatus.PENDING)
                .failureReason(failureReason)
                .build();
    }

    protected static void amountValidation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Invalid value entered. value={}", amount);
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    private static void validatingTransference(TransferenceRequestDTO dto) {
        if (dto.originAccountId().equals(dto.destinationAccountId())){
            log.warn("User informed identical accounts. originAccountId={}, destinationAccountId={} ", dto.originAccountId(), dto.destinationAccountId());
            throw new SameAccountException("The accounts must be different");

        }
        amountValidation(dto.amount());
    }

    private TransactionResponseDTO executeTransfer(
            Transaction transaction,
            TransferenceRequestDTO dto,
            AccountResponseDTO originAccount,
            AccountResponseDTO destinationAccount) {

        if (isAccountServiceUnavailable(() -> accountClient.debit(new WithdrawRequestDTO(originAccount.accountId(), dto.amount())))) {
            return markAsPending(transaction, FailureReason.DEBIT_FAILED, dto);
        }

        if (isAccountServiceUnavailable(() -> accountClient.credit(new DepositRequestDTO(destinationAccount.accountId(), dto.amount())))) {
            return markAsPending(transaction, FailureReason.CREDIT_FAILED, dto);
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        eventPublisher.publishTransferenceCompleted(transaction);

        log.info("Transference succeeded. originAccountId={}, destinationAccountId={}, value={}",
                dto.originAccountId(), dto.destinationAccountId(), dto.amount());

        return TransactionResponseDTO.transferenceCompletedResponse(transaction);
    }

    private boolean isAccountServiceUnavailable(Runnable operation) {
        try {
            operation.run();
            return false;
        } catch (AccountServiceUnavailableException | ServiceUnavailableException e) {
            log.warn("Account service unavailable: {}", e.getMessage());
            return true;
        }
    }

    private TransactionResponseDTO markAsPending(Transaction transaction, FailureReason reason, TransferenceRequestDTO dto) {
        transaction.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        pendingTransactionRepository.save(createPendingTransaction(transaction, reason));

        log.info("Transference pending. originAccountId={}, destinationAccountId={}, value={}",
                dto.originAccountId(), dto.destinationAccountId(), dto.amount());

        return TransactionResponseDTO.transferencePendingResponse(transaction);
    }


}
