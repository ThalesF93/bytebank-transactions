package br.com.bytebank.transactions.api.dtos.requests;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountOperationRequest {
    UUID accountId();
    BigDecimal amount();
}
