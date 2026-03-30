package br.com.bytebank.transactions.dtos.responses;



import br.com.bytebank.transactions.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        OperationType type,
        BigDecimal amount,
        String description,
        LocalDateTime dateTime

) {}
