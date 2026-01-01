package com.mahmoud.ledger.domain.model;

import java.util.UUID;

public class Posting {
    private final UUID accountId;
    private final Money amount;

    // In strict accounting, a Posting has a Debit or Credit direction.
    // However, if Money is signed (or if we use isDebit/isCredit), we can simplify.
    // Let's keep Type for clarity, but Money amount should generally be absolute OR
    // signed.
    // Convention:
    // If Type is DEBIT && Account is ASSET -> Increase.
    // For simplicity in this Value Object design, let's say:
    // This Posting represents a modification to the account.

    public enum Type {
        DEBIT, CREDIT
    }

    private final Type type;

    public Posting(UUID accountId, Money amount, Type type) {
        if (amount.isNegative()) {
            throw new IllegalArgumentException("Posting amount must be positive");
        }
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Money getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }
}
