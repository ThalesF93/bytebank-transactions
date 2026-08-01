package br.com.bytebank.transactions.infrastructure.feignclient;

import br.com.bytebank.transactions.domain.contract.AccountClientContract;
import br.com.bytebank.transactions.infrastructure.dtos.client.responses.AccountResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.client.responses.CustomerClientResponseDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.dtos.requests.WithdrawRequestDTO;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.AccountServiceUnavailableException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountClientImpl implements AccountClientContract {

    private final AccountClient accountClient;

    @Override
    public void debit(UUID accountId, BigDecimal amount) {
        WithdrawRequestDTO dto = new WithdrawRequestDTO(accountId, amount);
        try {
            accountClient.debit(dto);
        } catch (FeignException e) {
            System.out.println(e.getMessage());
            throw new AccountServiceUnavailableException();
        }
    }

    @Override
    public void credit(UUID accountId, BigDecimal amount) {
        DepositRequestDTO dto = new DepositRequestDTO(accountId, amount);
        try {
            accountClient.credit(dto);
        } catch (FeignException e) {
            System.out.println(e.getMessage());
            throw new AccountServiceUnavailableException();

        }
    }

    @Override
    public AccountResponseDTO findAccount(UUID id) {
        return accountClient.findAccount(id);
    }

    @Override
    public CustomerClientResponseDTO findCustomerByAccountId(UUID id) {
        return accountClient.findCustomerByAccountId(id);
    }

    @Override
    public AccountResponseDTO findAccountByCustomerId(UUID customerId) {
        return accountClient.findAccountByCustomerId(customerId);
    }
}
