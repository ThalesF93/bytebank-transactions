package br.com.bytebank.transactions.application.service;

import br.com.bytebank.transactions.infrastructure.api.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.responses.TransactionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    List<BankStatementResponseDTO> generateBankStatement(UUID id);

    TransactionResponseDTO getTransactionById(UUID id);
}
