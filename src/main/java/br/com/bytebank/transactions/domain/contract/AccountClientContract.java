package br.com.bytebank.transactions.domain.contract;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountClientContract {
    void debit(UUID accountId, BigDecimal amount);
    void credit(UUID accountId, BigDecimal amount);

}
