package br.com.bytebank.transactions.infrastructure.feignclient;

import br.com.bytebank.transactions.api.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.api.dtos.client.responses.CustomerClientResponseDTO;
import br.com.bytebank.transactions.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.api.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "bytebank-accounts",
path = "/api/v1/accounts",
configuration = FeignConfig.class)
public interface AccountClient {

    @GetMapping("/{id}")
    AccountResponseDTO findAccount(@PathVariable UUID id);

    @PostMapping("/debit")
    Void debit(@RequestBody WithdrawRequestDTO dto);

    @PostMapping("/credit")
    Void credit (@RequestBody DepositRequestDTO dto);

    @GetMapping("/feign/customer/{id}")
    CustomerClientResponseDTO findCustomerByAccountId(@PathVariable UUID id);

}
