package br.com.bytebank.transactions.infrastructure.feignclient;

import br.com.bytebank.transactions.domain.contract.AccountClientContract;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.DepositRequestDTO;
import br.com.bytebank.transactions.infrastructure.api.dtos.requests.WithdrawRequestDTO;
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
        accountClient.debit(dto);
    }

    @Override
    public void credit(UUID accountId, BigDecimal amount) {
        DepositRequestDTO dto = new DepositRequestDTO(accountId, amount);
        accountClient.credit(dto);
    }
}
