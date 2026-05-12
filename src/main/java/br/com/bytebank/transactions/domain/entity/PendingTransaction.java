package br.com.bytebank.transactions.domain.entity;

import br.com.bytebank.transactions.domain.enums.FailureReason;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_transactions")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingTransaction {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction sourceTransaction;

    @Column(name = "origin_account_id")
    private UUID originAccountId;

    @Column(name = "destination_account_id")
    private UUID destinationAccountId;

    @Column
    @Positive
    private BigDecimal amount;

    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "transaction_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(name = "failure_reason")
    @Enumerated(EnumType.STRING)
    private FailureReason failureReason;

    @Column
    private LocalDateTime dateTime;

    @Column
    private int attempts;

    @Column
    private boolean processed;


}
