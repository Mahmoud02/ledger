package com.mahmoud.ledger.application.port.in;

import com.mahmoud.ledger.domain.model.AccountType;
import java.math.BigDecimal;

public record CreateAccountCommand(String name, AccountType type, String currency) {
    public CreateAccountCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Account type cannot be null");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
    }
}
