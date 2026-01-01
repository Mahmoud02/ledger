package com.mahmoud.ledger.application.port.in;

import com.mahmoud.ledger.domain.model.Account;
import java.util.UUID;

public interface RetrieveAccountUseCase {
    Account getAccount(UUID accountId);
}
