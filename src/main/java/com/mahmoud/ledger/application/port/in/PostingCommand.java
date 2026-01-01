package com.mahmoud.ledger.application.port.in;

import com.mahmoud.ledger.domain.model.Posting;
import java.math.BigDecimal;
import java.util.UUID;

public record PostingCommand(UUID accountId, BigDecimal amount, String currency, Posting.Type type) {
}
