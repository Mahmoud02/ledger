package com.mahmoud.ledger.infrastructure.persistence;

import com.mahmoud.ledger.domain.model.Money;
import com.mahmoud.ledger.domain.model.Posting;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "postings")
@Getter
@Setter
@NoArgsConstructor
public class PostingJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID accountId;

    // Manual embedding for Money
    private BigDecimal amount;
    private String currency; // ISO code

    @Enumerated(EnumType.STRING)
    private Posting.Type type;

    public static PostingJpaEntity fromDomain(Posting posting) {
        PostingJpaEntity entity = new PostingJpaEntity();
        entity.setAccountId(posting.getAccountId());
        entity.setAmount(posting.getAmount().amount());
        entity.setCurrency(posting.getAmount().currency().getCurrencyCode());
        entity.setType(posting.getType());
        return entity;
    }

    public Posting toDomain() {
        return new Posting(accountId, Money.of(amount, currency), type);
    }
}
