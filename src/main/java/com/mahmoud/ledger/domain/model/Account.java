package com.mahmoud.ledger.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account {
    private final UUID id;
    private final String name;
    private final AccountType type;
    private Money balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;

    public Account(UUID id, String name, AccountType type, Money balance, AccountStatus status,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Account create(UUID id, String name, AccountType type, String currencyCode) {
        // Initialize with ZERO money in the given currency
        return new Account(id, name, type, Money.of(BigDecimal.ZERO, currencyCode),
                AccountStatus.ACTIVE,
                LocalDateTime.now());
    }

    public void postPosting(Posting posting) {
        if (!posting.getAmount().currency().equals(this.balance.currency())) {
            throw new IllegalArgumentException("Posting currency mismatch");
        }

        // Update balance logic based on Account Type
        if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
            // Normal (Asset) Behavior: Debit +, Credit -
            if (posting.getType() == Posting.Type.DEBIT) {
                this.balance = this.balance.add(posting.getAmount());
            } else {
                Money newBalance = this.balance.subtract(posting.getAmount());
                if (newBalance.amount().signum() < 0) {
                    throw new IllegalStateException("Insufficient funds");
                }
                this.balance = newBalance;
            }
        } else {
            // Inverted (Equity/Liability) Behavior: Credit +, Debit -
            if (posting.getType() == Posting.Type.CREDIT) {
                this.balance = this.balance.add(posting.getAmount());
            } else {
                Money newBalance = this.balance.subtract(posting.getAmount());
                if (newBalance.amount().signum() < 0) {
                    throw new IllegalStateException("Insufficient funds");
                }
                this.balance = newBalance;
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
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
