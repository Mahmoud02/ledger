package com.mahmoud.ledger;

import com.mahmoud.ledger.application.port.in.CreateAccountCommand;
import com.mahmoud.ledger.application.port.in.TransferFundsCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LedgerIntegrationTest {

    @Autowired
    private RestTestClient restClient;

    @Test
    void given_SenderAndReceiverAccounts_When_TransferFunds_Then_BalancesAreUpdated() {
        // 1. Create Alice
        CreateAccountCommand aliceCmd = new CreateAccountCommand("Alice", "USD");
        UUID aliceId = restClient.post().uri("/api/accounts")
                .body(aliceCmd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UUID.class)
                .returnResult().getResponseBody();

        assertThat(aliceId).isNotNull();

        // 2. Create Bob
        CreateAccountCommand bobCmd = new CreateAccountCommand("Bob", "USD");
        UUID bobId = restClient.post().uri("/api/accounts")
                .body(bobCmd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UUID.class)
                .returnResult().getResponseBody();

        assertThat(bobId).isNotNull();

        // 3. Alice verifies initial balance (0)
        verifyBalance(aliceId, new BigDecimal("0.00"));

        // 4. Transfer 50 USD from Alice to Bob
        TransferFundsCommand transferCmd = new TransferFundsCommand(aliceId, bobId, new BigDecimal("50.00"), "USD",
                "E2E Transfer");
        restClient.post().uri("/api/transfers")
                .body(transferCmd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UUID.class);

        // 5. Verify Final Balances
        verifyBalance(aliceId, new BigDecimal("-50.00"));
        verifyBalance(bobId, new BigDecimal("50.00"));
    }

    private void verifyBalance(UUID accountId, BigDecimal expectedAmount) {
        restClient.get().uri("/api/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountResponse.class)
                .value(response -> assertThat(response.balance()).isEqualTo(expectedAmount));
    }

    record AccountResponse(UUID id, String name, String currency, BigDecimal balance, String status) {
    }
}
