package com.mahmoud.ledger;

import com.mahmoud.ledger.application.port.in.CreateAccountCommand;
import com.mahmoud.ledger.application.port.in.TransferFundsCommand;
import com.mahmoud.ledger.application.port.in.CreateAccountUseCase;
import com.mahmoud.ledger.application.port.in.RetrieveAccountUseCase;
import com.mahmoud.ledger.application.port.in.TransferFundsUseCase;
import com.mahmoud.ledger.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LedgerIntegrationTest {

    @Autowired
    private CreateAccountUseCase createAccountUseCase;

    @Autowired
    private TransferFundsUseCase transferFundsUseCase;

    @Autowired
    private RetrieveAccountUseCase retrieveAccountUseCase;

    @Test
    void testFullTransferFlow() {
        // 1. Create Accounts
        UUID aliceId = createAccountUseCase.createAccount(new CreateAccountCommand("Alice", "USD"));
        UUID bobId = createAccountUseCase.createAccount(new CreateAccountCommand("Bob", "USD"));

        // 2. Transfer (Alice has 0, so she will go negative)
        // Asset Logic: Credit Alice (-50), Debit Bob (+50)
        TransferFundsCommand cmd = new TransferFundsCommand(aliceId, bobId, new BigDecimal("50.00"), "USD",
                " Integration Transfer");

        UUID txId = transferFundsUseCase.transferFunds(cmd);
        assertNotNull(txId);

        // 3. Verify Balances
        Account alice = retrieveAccountUseCase.getAccount(aliceId);
        Account bob = retrieveAccountUseCase.getAccount(bobId);

        assertEquals(new BigDecimal("-50.00"), alice.getBalance().amount());
        assertEquals(new BigDecimal("50.00"), bob.getBalance().amount());
    }
}
