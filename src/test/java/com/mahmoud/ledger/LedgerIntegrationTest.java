package com.mahmoud.ledger;

import com.mahmoud.ledger.application.port.in.CreateAccountCommand;
import com.mahmoud.ledger.application.port.in.PostTransactionCommand;
import com.mahmoud.ledger.application.port.in.PostingCommand;
import com.mahmoud.ledger.application.port.in.TransferFundsCommand;
import com.mahmoud.ledger.domain.model.AccountType;
import com.mahmoud.ledger.domain.model.Posting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
public class LedgerIntegrationTest {

        @Autowired
        private RestTestClient restClient;

        @Test
        void given_NewAccount_When_DepositFunds_Then_BalanceIsUpdated() {
                // 1. Create a "Genesis" account (Equity)
                CreateAccountCommand genesisCmd = new CreateAccountCommand("Genesis", AccountType.EQUITY, "USD");
                UUID genesisId = createAccountHelper(genesisCmd);

                // 2. Create the User's account (Asset)
                CreateAccountCommand userCmd = new CreateAccountCommand("Alice", AccountType.ASSET, "USD");
                UUID aliceId = createAccountHelper(userCmd);

                // 3. Deposit (Genesis -> Alice)
                PostTransactionCommand depositTransaction = new PostTransactionCommand(
                                "Deposit to Alice",
                                List.of(
                                                new PostingCommand(genesisId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.CREDIT),
                                                new PostingCommand(aliceId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.DEBIT)));

                restClient.post().uri("/api/transactions")
                                .body(depositTransaction)
                                .exchange()
                                .expectStatus().isOk();

                // 4. Verify Balance
                verifyBalance(aliceId, new BigDecimal("100.0"));
        }

        @Test
        void given_FundedAccounts_When_TransferFunds_Then_BalancesAreUpdated() {
                // 1. Create Accounts
                UUID genesisId = createAccountHelper(
                                new CreateAccountCommand("Genesis", AccountType.EQUITY, "USD"));
                UUID aliceId = createAccountHelper(new CreateAccountCommand("Alice", AccountType.ASSET, "USD"));
                UUID bobId = createAccountHelper(new CreateAccountCommand("Bob", AccountType.ASSET, "USD"));

                // 2. Fund Alice (Genesis -> Alice)
                PostTransactionCommand fundAlice = new PostTransactionCommand(
                                "Fund Alice",
                                List.of(
                                                new PostingCommand(genesisId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.CREDIT),
                                                new PostingCommand(aliceId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.DEBIT)));

                restClient.post().uri("/api/transactions")
                                .body(fundAlice)
                                .exchange()
                                .expectStatus().isOk();

                // 3. Transfer Alice -> Bob
                TransferFundsCommand transferCmd = new TransferFundsCommand(aliceId, bobId, new BigDecimal("40.00"),
                                "USD", "E2E Transfer");

                restClient.post().uri("/api/transfers")
                                .body(transferCmd)
                                .exchange()
                                .expectStatus().isOk();

                // 4. Verify Final Balances
                // Alice: 100 - 40 = 60
                verifyBalance(aliceId, new BigDecimal("60.0"));
                // Bob: 0 + 40 = 40
                verifyBalance(bobId, new BigDecimal("40.0"));
        }

        private UUID createAccountHelper(CreateAccountCommand cmd) {
                return restClient.post().uri("/api/accounts")
                                .body(cmd)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(UUID.class)
                                .returnResult().getResponseBody();
        }

        private void verifyBalance(UUID accountId, BigDecimal expectedAmount) {
                restClient.get().uri("/api/accounts/" + accountId)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.balance").isEqualTo(expectedAmount.doubleValue());
        }
}
