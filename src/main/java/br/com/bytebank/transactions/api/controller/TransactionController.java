package br.com.bytebank.transactions.api.controller;


import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.TransferenceRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.api.dtos.responses.BankStatementResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.DepositResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.api.dtos.responses.WithdrawResponseDTO;
import br.com.bytebank.transactions.application.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponseDTO> deposit(@Valid @RequestBody DepositRequestDTO depositRequestDTO){

        log.info("Request received. endpoint=POST /deposit value={}",depositRequestDTO.amount());

        var deposit = transactionService.deposit(depositRequestDTO);

        log.info("Deposit completed. accountID={}", depositRequestDTO.accountId());
        return ResponseEntity.status(HttpStatus.OK).body(deposit);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponseDTO> withdraw(@Valid @RequestBody WithdrawRequestDTO withdrawRequestDTO){

        log.info("Request received. endpoint=POST /withdraw value={}",withdrawRequestDTO.amount());

        var transaction = transactionService.withdraw(withdrawRequestDTO);

        log.info("Withdraw completed. accountID={}", withdrawRequestDTO.accountId());
        return ResponseEntity.status(HttpStatus.OK).body(transaction);
    }

    @PostMapping("/transference")
    public ResponseEntity<TransactionResponseDTO> transference(@Valid @RequestBody TransferenceRequestDTO transferenceRequestDTO){
        log.info("Transference request received. endpoint=POST  value={}",transferenceRequestDTO.amount());
        var transference = transactionService.transference(transferenceRequestDTO);
        log.info("Transference done successfully. value={}",transferenceRequestDTO.amount());
        return ResponseEntity.ok(transference);
    }

    @GetMapping("/statement/{id}")
    public ResponseEntity<List<BankStatementResponseDTO>> getStatement(@PathVariable UUID id){

        var transactions = transactionService.generateBankStatement(id);
        log.info("Statement generated from accountID id={}", id);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable UUID id){
        log.info("Found AccountID id={}", id);
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

}
