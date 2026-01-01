package com.mahmoud.ledger.application.port.out;

import com.mahmoud.ledger.domain.model.Transaction;

public interface TransactionPort {
    Transaction save(Transaction transaction);
}
