package br.com.bytebank.transactions.application.service;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponseDTO withdraw(WithdrawRequestDTO requestDTO);

    TransactionResponseDTO deposit(DepositRequestDTO requestDTO);

    void transference(UUID originAccountId, UUID destinationAccountId, BigDecimal amount);

    public List<TransactionResponseDTO> generateBankStatement(UUID id);
}
