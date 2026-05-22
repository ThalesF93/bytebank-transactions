package br.com.bytebank.transactions.api.openapi.controller;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.WithdrawResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

public interface TransactionControllerOpenApi {

    ResponseEntity<DepositResponseDTO> deposit(@Valid @RequestBody DepositRequestDTO depositRequestDTO);

    ResponseEntity<WithdrawResponseDTO> withdraw(@Valid @RequestBody WithdrawRequestDTO withdrawRequestDTO);

    ResponseEntity<TransactionResponseDTO> transference(@Valid @RequestBody TransferenceRequestDTO transferenceRequestDTO);

    ResponseEntity<List<BankStatementResponseDTO>> getStatement(@PathVariable UUID id);

    ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable UUID id);
}
