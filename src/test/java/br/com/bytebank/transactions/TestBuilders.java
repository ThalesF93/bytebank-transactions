package br.com.bytebank.transactions;

import br.com.bytebank.transactions.api.dtos.requests.AccountOperationRequest;
import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestBuilders {

    public static DepositRequestDTO depositRequestDTO(){
        return new DepositRequestDTO(
                UUID.randomUUID(), new BigDecimal("10")
        );
    }

    public static WithdrawRequestDTO withdrawRequestDTO(){
        return new WithdrawRequestDTO(
                UUID.randomUUID(), new BigDecimal("10")
        );
    }
    public static Transaction createWithdrawTransaction(WithdrawRequestDTO dto){
        Transaction transaction = new Transaction();
        transaction.setOriginAccountId(dto.accountId());;
        transaction.setType(OperationType.WITHDRAW);
        transaction.setAmount(dto.amount());
        transaction.setDateTime(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PROCESSING);
        return transaction;
    }

    public static Transaction createDepositTransaction(DepositRequestDTO dto){
        Transaction transaction = new Transaction();
        transaction.setOriginAccountId(dto.accountId());;
        transaction.setType(OperationType.DEPOSIT);
        transaction.setAmount(dto.amount());
        transaction.setDateTime(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PROCESSING);
        return transaction;
    }

    public static TransferenceRequestDTO createTransferenceDTO(){
        return new TransferenceRequestDTO(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("150.00"));
    }

    public static Transaction createTransferenceEntity(AccountOperationRequest requestDTO){
        Transaction transaction = new Transaction();
        transaction.setOriginAccountId(requestDTO.accountId());;
        transaction.setType(OperationType.TRANSFER);
        transaction.setAmount(requestDTO.amount());
        transaction.setDateTime(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PROCESSING);
        return transaction;
    }
}
