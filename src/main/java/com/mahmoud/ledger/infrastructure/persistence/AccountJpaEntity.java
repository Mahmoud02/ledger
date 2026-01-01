package com.mahmoud.ledger.infrastructure.persistence;

import com.mahmoud.ledger.domain.model.Account;
import com.mahmoud.ledger.domain.model.AccountStatus;
import com.mahmoud.ledger.domain.model.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountJpaEntity {
    @Id
    private UUID id;

    private String name;
    private String currency;
    private BigDecimal balanceAmount; // Snapshot

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private LocalDateTime createdAt;

    // All-args constructor for fromDomain mapping
    public AccountJpaEntity(UUID id, String name, String currency, BigDecimal balanceAmount, AccountStatus status,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.balanceAmount = balanceAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static AccountJpaEntity fromDomain(Account account) {
        return new AccountJpaEntity(
                account.getId(),
                account.getName(),
                account.getBalance().currency().getCurrencyCode(),
                account.getBalance().amount(),
                account.getStatus(),
                account.getCreatedAt());
    }

    public Account toDomain() {
        return new Account(id, name, Money.of(balanceAmount, currency), status, createdAt);
    }
}
