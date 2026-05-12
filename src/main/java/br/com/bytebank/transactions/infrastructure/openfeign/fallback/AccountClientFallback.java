//package br.com.bytebank.transactions.infrastructure.openfeign.fallback;
//
//
//
//import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
//import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
//import br.com.bytebank.transactions.domain.exception.AccountServiceUnavailableException;
//import br.com.bytebank.transactions.domain.exception.TransactionException;
//import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
//import br.com.bytebank.transactions.infrastructure.openfeign.dtos.responses.AccountResponseDTO;
//import br.com.bytebank.transactions.infrastructure.repositories.PendingTransactionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import javax.security.auth.login.AccountNotFoundException;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class AccountClientFallback implements AccountClient {
//
//    @Override
//    public Void debit(WithdrawRequestDTO dto) {
////        log.error("Debit fallback triggered. Account service unavailable. accountId={}", dto.accountId());
////        throw new TransactionException("Account service unavailable for debit");
//    }
//
//    @Override
//    public Void credit(DepositRequestDTO dto) {
//        log.error("Credit fallback triggered. Account service unavailable. accountId={}", dto.accountId());
//        throw new TransactionException("Account service unavailable for credit");
//    }
//
//    @Override
//    public AccountResponseDTO findAccount(UUID id) {
//        log.error("FindAccount fallback triggered. Account service unavailable. accountId={}", id);
//        throw new AccountServiceUnavailableException("Account service unavailable");
//    }
//}
