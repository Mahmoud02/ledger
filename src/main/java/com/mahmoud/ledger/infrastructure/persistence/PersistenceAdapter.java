package com.mahmoud.ledger.infrastructure.persistence;

import com.mahmoud.ledger.application.port.out.AccountPort;
import com.mahmoud.ledger.application.port.out.TransactionPort;
import com.mahmoud.ledger.domain.model.Account;
import com.mahmoud.ledger.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PersistenceAdapter implements AccountPort, TransactionPort {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = AccountJpaEntity.fromDomain(account);
        AccountJpaEntity saved = accountRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Account> load(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(AccountJpaEntity::toDomain);
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = TransactionJpaEntity.fromDomain(transaction);
        TransactionJpaEntity saved = transactionRepository.save(entity);
        return saved.toDomain();
    }
}
