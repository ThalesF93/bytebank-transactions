package br.com.bytebank.transactions.infrastructure.dtos.requests;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountOperationRequest {
    UUID accountId();
    BigDecimal amount();
}
