package com.mahmoud.ledger.infrastructure.persistence;

import com.mahmoud.ledger.domain.model.Account;
import com.mahmoud.ledger.domain.model.AccountStatus;
import com.mahmoud.ledger.domain.model.AccountType;
import com.mahmoud.ledger.domain.model.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal balanceAmount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Default constructor for JPA
    protected AccountJpaEntity() {
    }

    public AccountJpaEntity(UUID id, String name, BigDecimal balanceAmount, String currency, AccountStatus status,
            AccountType type, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.balanceAmount = balanceAmount;
        this.currency = currency;
        this.status = status;
        this.type = type;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public AccountType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static AccountJpaEntity fromDomain(Account account) {
        return new AccountJpaEntity(
                account.getId(),
                account.getName(),
                account.getBalance().amount(),
                account.getBalance().currency().getCurrencyCode(),
                account.getStatus(),
                account.getType(),
                account.getCreatedAt());
    }

    public Account toDomain() {
        return new Account(id, name, type, Money.of(balanceAmount, currency), status, createdAt);
    }
}
