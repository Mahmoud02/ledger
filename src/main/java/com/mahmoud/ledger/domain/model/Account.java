package com.mahmoud.ledger.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account {
    private final UUID id;
    private final String name;
    private Money balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;

    public Account(UUID id, String name, Money balance, AccountStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Account create(UUID id, String name, String currencyCode) {
        // Initialize with ZERO money in the given currency
        return new Account(id, name, Money.of(BigDecimal.ZERO, currencyCode), AccountStatus.ACTIVE,
                LocalDateTime.now());
    }

    public void postPosting(Posting posting) {
        if (!posting.getAmount().currency().equals(this.balance.currency())) {
            throw new IllegalArgumentException("Posting currency mismatch");
        }

        // Update balance logic:
        // Asset Account: Debit increases, Credit decreases.
        // We'll assume these are Asset accounts for now.
        if (posting.getType() == Posting.Type.DEBIT) {
            this.balance = this.balance.add(posting.getAmount());
        } else {
            this.balance = this.balance.subtract(posting.getAmount());
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
