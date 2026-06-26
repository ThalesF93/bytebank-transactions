package br.com.bytebank.transactions.domain.contract;

import br.com.bytebank.transactions.infrastructure.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.client.responses.CustomerClientResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountClientContract {
    void debit(UUID accountId, BigDecimal amount);
    void credit(UUID accountId, BigDecimal amount);
    AccountResponseDTO findAccount(@PathVariable UUID id);
    CustomerClientResponseDTO findCustomerByAccountId(@PathVariable UUID id);
    AccountResponseDTO findAccountByCustomerId(@PathVariable UUID customerId);

}
