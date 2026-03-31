package br.com.bytebank.transactions.controllers;


import br.com.bytebank.transactions.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.dtos.responses.TransactionResponseDTO;
import br.com.bytebank.transactions.services.TransactionService;
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

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> deposit(@Valid @RequestBody DepositRequestDTO depositRequestDTO){

        log.info("Request received. endpoint=PATCH /deposit value={}",depositRequestDTO.amount());

        var deposit = transactionService.deposit(depositRequestDTO);

        log.info("Deposit completed. accountID={}", depositRequestDTO.accountId());
        return ResponseEntity.status(HttpStatus.OK).body(deposit);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdraw(@Valid @RequestBody WithdrawRequestDTO withdrawRequestDTO){

        log.info("Request received. endpoint=PATCH /withdraw value={}",withdrawRequestDTO.amount());

        var transaction = transactionService.withdraw(withdrawRequestDTO);

        log.info("Withdraw completed. accountID={}", withdrawRequestDTO.accountId());
        return ResponseEntity.status(HttpStatus.OK).body(transaction);
    }

//    @PostMapping
//    public ResponseEntity<String> transference(@Valid @RequestBody TransferenceRequestDTO transferenceRequestDTO){
//        log.info("Transference request received. endpoint=POST  value={}",transferenceRequestDTO.amount());
//        transactionService.transference(
//                transferenceRequestDTO.originAccountId(),
//                transferenceRequestDTO.destinationAccountId(),
//                transferenceRequestDTO.amount());
//        log.info("Transference done successfully. value={}",transferenceRequestDTO.amount());
//        return ResponseEntity.status(HttpStatus.OK).body("Operation successfully done");
//    }
//
//    @GetMapping("/{id}/transactions")
//    public ResponseEntity<List<TransactionResponseDTO>> getStatements(@PathVariable UUID id){
//
//        var transactions = accountService.generateBankStatement(id);
//        return ResponseEntity.status(HttpStatus.OK).body(transactions);
//    }

}
