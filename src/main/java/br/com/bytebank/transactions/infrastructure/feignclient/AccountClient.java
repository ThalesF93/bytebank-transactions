package br.com.bytebank.transactions.infrastructure.feignclient;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.config.FeignConfig;
import br.com.bytebank.transactions.infrastructure.openfeign.dtos.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.openfeign.fallback.AccountClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "bytebank-accounts",
path = "api/v1/accounts",
fallback = AccountClientFallback.class,
configuration = FeignConfig.class)
public interface AccountClient {

    @GetMapping("/{id}")
    AccountResponseDTO findAccount(@PathVariable UUID id);

    @PostMapping("/debit")
    Void debit(@RequestBody WithdrawRequestDTO dto);

    @PostMapping("/credit")
    Void credit (@RequestBody DepositRequestDTO dto);



}
