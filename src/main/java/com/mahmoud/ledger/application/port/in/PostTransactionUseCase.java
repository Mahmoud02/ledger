package com.mahmoud.ledger.application.port.in;

import com.mahmoud.ledger.domain.model.Posting;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PostTransactionUseCase {
    UUID postTransaction(PostTransactionCommand command);
}
