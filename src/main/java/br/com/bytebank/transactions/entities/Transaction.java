package br.com.bytebank.transactions.entities;


import br.com.coderbank.operacoes_bancarias.enums.OperationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "transactions")
@RequiredArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
    // mudar para id de saida e entrada

    @Enumerated(EnumType.STRING)
    @Column
    private OperationType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column
    private LocalDateTime dateTime;


    @Column
    private String description;


    public Transaction(OperationType type, BigDecimal amount, String description) {
        this.type = type;
        this.amount = amount;
        this.description = description;
    }


    public Transaction(OperationType type, BigDecimal amount) {
        this.type = type;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Transaction: " + type +
                ", Amount: $ " + amount +
                ", Date: " + dateTime +
                '\n';
    }
}


