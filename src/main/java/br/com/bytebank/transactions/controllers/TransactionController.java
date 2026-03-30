package br.com.bytebank.transactions.controllers;

import br.com.coderbank.operacoes_bancarias.dtos.transacoes.requests.DepositRequestDTO;
import br.com.coderbank.operacoes_bancarias.dtos.transacoes.requests.TransferenceRequestDTO;
import br.com.coderbank.operacoes_bancarias.dtos.transacoes.requests.WithdrawRequestDTO;
import br.com.coderbank.operacoes_bancarias.services.transacoes.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PatchMapping("/deposit")
    public ResponseEntity<String> deposit(@Valid @RequestBody DepositRequestDTO depositRequestDTO){

        log.info("Request received. endpoint=PATCH /deposit value={}",depositRequestDTO.amount());

        transactionService.deposit(depositRequestDTO.accountId(), depositRequestDTO.amount());

        log.info("Deposit completed. accountID={}", depositRequestDTO.accountId());
        return ResponseEntity.status(HttpStatus.OK).body("Operation successfully done");
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<String> withdraw(@Valid @RequestBody WithdrawRequestDTO withdrawRequestDTO){

        log.info("Request received. endpoint=PATCH /withdraw value={}",withdrawRequestDTO.amount());

        transactionService.withdraw(withdrawRequestDTO.accountId(), withdrawRequestDTO.amount());

        log.info("Withdraw completed. accountID={}", withdrawRequestDTO.accountId());
        return ResponseEntity.status(HttpStatus.OK).body("Operation successfully done");
    }

    @PostMapping
    public ResponseEntity<String> transference(@Valid @RequestBody TransferenceRequestDTO transferenceRequestDTO){
        log.info("Transference request received. endpoint=POST  value={}",transferenceRequestDTO.amount());
        transactionService.transference(
                transferenceRequestDTO.originAccountId(),
                transferenceRequestDTO.destinationAccountId(),
                transferenceRequestDTO.amount());
        log.info("Transference done successfully. value={}",transferenceRequestDTO.amount());
        return ResponseEntity.status(HttpStatus.OK).body("Operation successfully done");
    }


}
