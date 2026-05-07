package br.com.bytebank.transactions.infrastructure.openfeign.fallback;



import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.feignclient.AccountClient;
import br.com.bytebank.transactions.infrastructure.openfeign.dtos.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.repositories.PendingTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountClientFallback implements AccountClient {


    private final PendingTransactionRepository repository;


    @Override
    public ResponseEntity<AccountResponseDTO> findAccount(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> debit(WithdrawRequestDTO dto) {
        return null;
    }

    @Override
    public ResponseEntity<Void> credit(DepositRequestDTO dto) {
        return null;
    }
}
