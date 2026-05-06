package br.com.bytebank.transactions.infrastructure.feignclient;

import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service",
url = "http://localhost:8082",
path = "api/v1/accounts")
//fallback = AccountClienteFallback.class,
//configuration = FeignConfig.class)
public interface AccountClient {

    @PostMapping("/debit")
    ResponseEntity<Void> debit(@RequestBody WithdrawRequestDTO dto);

    @PostMapping("/credit")
    ResponseEntity<Void> credit (@RequestBody DepositRequestDTO dto);



}
