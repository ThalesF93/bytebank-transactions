package br.com.bytebank.transactions.application.service;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.WithdrawResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    WithdrawResponseDTO withdraw(WithdrawRequestDTO requestDTO);

    DepositResponseDTO deposit(DepositRequestDTO requestDTO);

    TransactionResponseDTO transference(TransferenceRequestDTO dto);

    List<TransactionResponseDTO> generateBankStatement(UUID id);
}
