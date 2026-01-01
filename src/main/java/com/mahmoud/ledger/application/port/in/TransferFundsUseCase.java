package com.mahmoud.ledger.application.port.in;

import java.util.UUID;

public interface TransferFundsUseCase {
    UUID transferFunds(TransferFundsCommand command);
}
