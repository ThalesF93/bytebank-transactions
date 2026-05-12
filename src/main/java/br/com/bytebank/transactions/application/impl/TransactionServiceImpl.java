package br.com.bytebank.transactions.application.impl;

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
import br.com.bytebank.transactions.domain.exception.*;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.openfeign.dtos.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.repositories.PendingTransactionRepository;
import br.com.bytebank.transactions.infrastructure.repositories.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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


    public WithdrawResponseDTO withdraw(WithdrawRequestDTO requestDTO){

        amountValidation(requestDTO.amount());

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


    public DepositResponseDTO deposit(DepositRequestDTO requestDTO){

        amountValidation(requestDTO.amount());

        Transaction transaction = createTransactionEntity(requestDTO, OperationType.DEPOSIT, TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        try {
            accountClient.credit(new DepositRequestDTO(requestDTO.accountId(), requestDTO.amount()));
            transaction.setStatus(TransactionStatus.COMPLETED);
            log.info("Deposit succeeded. accountId={}, value={}", requestDTO.accountId(), requestDTO.amount());
            transactionRepository.save(transaction);

        } catch (FeignException e) {
            PendingTransaction pendingTransaction = createPendingTransaction(transaction, FailureReason.CREDIT_FAILED);
            pendingTransactionRepository.save(pendingTransaction);
        }

        return DepositResponseDTO.response(transaction);
    }

    @Override
    public TransactionResponseDTO transference(TransferenceRequestDTO dto){

        amountValidation(dto.amount());

        ResponseEntity<AccountResponseDTO> originAccount = null;
        ResponseEntity<AccountResponseDTO> destinationAccount = null;

        try {
            originAccount = accountClient.findAccount(dto.originAccountId());
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException("Origin account not found: " + dto.originAccountId());
        }catch (AccountServiceUnavailableException e){
            throw e;
        }

        try {
            destinationAccount = accountClient.findAccount(dto.destinationAccountId());
        } catch (FeignException e) {
            throw new AccountNotFoundException ("Destination account not found: " + dto.destinationAccountId());
        }catch (AccountServiceUnavailableException e){
            throw e;
        }

        if (dto.originAccountId().equals(dto.destinationAccountId())){
            log.warn("User informed identical accounts. originAccountId={}, destinationAccountId={} ", dto.originAccountId(), dto.destinationAccountId());
            throw new SameAccountException("The accounts must be different");

        }

        Transaction transaction = createTransactionEntity(dto, OperationType.TRANSFER, TransactionStatus.PROCESSING);
        transaction.setTargetAccountId(dto.destinationAccountId());
        transactionRepository.save(transaction);

        try {
            accountClient.debit(new WithdrawRequestDTO(originAccount.getBody().accountId(), dto.amount()));
            log.info("Debit succeeded into accountId={}", originAccount.getBody().accountId());
            try {
                accountClient.credit(new DepositRequestDTO(destinationAccount.getBody().accountId(), dto.amount() ));

                transaction.setStatus(TransactionStatus.COMPLETED);
                log.info("Transference succeeded. originAccountId={}, destinationAccountId={}, value={}", dto.originAccountId(), dto.destinationAccountId(), dto.amount());
                transactionRepository.save(transaction);

                return TransactionResponseDTO.transferenceCompletedResponse(transaction);

            }catch (FeignException e){

                PendingTransaction pendingTransaction = createPendingTransaction(transaction, FailureReason.CREDIT_FAILED);

                pendingTransactionRepository.save(pendingTransaction);
                log.info("Transference is pending. originAccountId={}, destinationAccountId={}, value={}", dto.originAccountId(), dto.destinationAccountId(), dto.amount());

                return TransactionResponseDTO.transferencePendingResponse(transaction);
            }
        } catch (FeignException e) {

            log.error("Debit still pending. error={}", e.getMessage());

            PendingTransaction pendingTransaction = createPendingTransaction(transaction, FailureReason.DEBIT_FAILED);

            pendingTransactionRepository.save(pendingTransaction);
            return TransactionResponseDTO.transferencePendingResponse(transaction);
        }

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
                ()-> new TransactionException("Transaction not found. ID= " + id)
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

}
