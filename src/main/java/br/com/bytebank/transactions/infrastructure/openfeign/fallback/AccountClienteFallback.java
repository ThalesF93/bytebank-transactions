//package br.com.bytebank.transactions.openfeign.fallback;
//
//
//import br.com.bytebank.customers.entity.PendingAccountOpening;
//import br.com.bytebank.customers.openfeign.dtos.requests.AccountRequestDTO;
//import br.com.bytebank.customers.openfeign.feignclient.AccountClient;
//import br.com.bytebank.customers.repositories.PendingAccountRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class AccountClienteFallback implements AccountClient {
//
//
//    private final PendingAccountRepository repository;
//
//    @Override
//    public ResponseEntity<Void> openAccount(AccountRequestDTO request) {
//        PendingAccountOpening pending = new PendingAccountOpening();
//        pending.setClientId(request.customerId());
//        pending.setAttempts(0);
//        pending.setProcessed(false);
//        repository.save(pending);
//        return ResponseEntity.ok().build();
//    }
//
//
//}
