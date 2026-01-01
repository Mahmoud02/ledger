package com.mahmoud.ledger.application.port.out;

import com.mahmoud.ledger.domain.model.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountPort {
    Account save(Account account);

    Optional<Account> load(UUID accountId);

    Optional<Account> loadLocked(UUID accountId);
}
