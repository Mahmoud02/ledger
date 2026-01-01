package com.mahmoud.ledger.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.mahmoud.ledger.domain.model.Money;
import com.mahmoud.ledger.domain.model.Posting;

public class Transaction {
    private final UUID id;
    private final LocalDateTime timestamp;
    private final String description;
    private final List<Posting> postings;

    public Transaction(UUID id, String description, LocalDateTime timestamp) {
        this.id = id;
        this.description = description;
        this.timestamp = timestamp;
        this.postings = new ArrayList<>();
    }

    public static Transaction create(String description) {
        return new Transaction(UUID.randomUUID(), description, LocalDateTime.now());
    }

    public void addPosting(Posting posting) {
        // Validation: Verify currency consistency?
        if (!postings.isEmpty()) {
            Money first = postings.get(0).getAmount();
            if (!first.currency().equals(posting.getAmount().currency())) {
                throw new IllegalArgumentException("Mixed currencies in a single transaction not supported yet");
            }
        }
        this.postings.add(posting);
    }

    public void validate() {
        if (postings.isEmpty()) {
            throw new IllegalStateException("Transaction must have postings");
        }

        // Sum debits and credits
        // Convention: Debit is positive, Credit is negative (or vice versa for
        // balancing)
        // If we sum them up:
        // sum = (Debit amounts) - (Credit amounts) should be ZERO.

        Money total = postings.stream()
                .map(p -> p.getType() == Posting.Type.DEBIT ? p.getAmount()
                        : new Money(p.getAmount().amount().negate(), p.getAmount().currency()))
                .reduce(new Money(BigDecimal.ZERO, postings.get(0).getAmount().currency()), Money::add);

        if (total.amount().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Transaction postings must sum to zero. Current imbalance: " + total.amount());
        }
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public List<Posting> getPostings() {
        return Collections.unmodifiableList(postings);
    }
}
