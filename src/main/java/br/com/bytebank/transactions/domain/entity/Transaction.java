package br.com.bytebank.transactions.domain.entity;


import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "transactions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private UUID originAccountId;

    @Column
    private UUID targetAccountId;

    @Enumerated(EnumType.STRING)
    @Column
    private OperationType type;

    @Enumerated(EnumType.STRING)
    @Column
    private TransactionStatus status;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column
    private LocalDateTime dateTime;

    @Column
    private String description;

}


