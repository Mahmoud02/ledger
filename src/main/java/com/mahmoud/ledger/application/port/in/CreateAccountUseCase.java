package com.mahmoud.ledger.application.port.in;

import java.util.UUID;

public interface CreateAccountUseCase {
    UUID createAccount(CreateAccountCommand command);
}
