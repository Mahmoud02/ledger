package com.mahmoud.ledger.application.port.in;

import java.util.List;

public record PostTransactionCommand(String description, List<PostingCommand> postings) {
    public PostTransactionCommand {
        if (postings == null || postings.isEmpty())
            throw new IllegalArgumentException("Postings cannot be empty");
    }
}
