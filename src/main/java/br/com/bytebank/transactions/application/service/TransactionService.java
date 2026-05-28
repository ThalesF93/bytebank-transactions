package br.com.bytebank.transactions.application.service;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.WithdrawResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.rmi.server.UID;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    WithdrawResponseDTO withdraw(UUID idempotencyKey , WithdrawRequestDTO requestDTO) throws JsonProcessingException;

    DepositResponseDTO deposit(UUID idempotencyKey ,DepositRequestDTO requestDTO) throws JsonProcessingException;

    TransactionResponseDTO transference(UUID idempotencyKey , TransferenceRequestDTO dto) throws JsonProcessingException;

    List<BankStatementResponseDTO> generateBankStatement(UUID id);

    TransactionResponseDTO getTransactionById(UUID id);
}
