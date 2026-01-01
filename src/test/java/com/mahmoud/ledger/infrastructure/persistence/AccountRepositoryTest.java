package com.mahmoud.ledger.infrastructure.persistence;

import com.mahmoud.ledger.domain.model.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void given_PersistedAccount_When_FindByIdLocked_Then_ReturnsAccount() {
        // Given
        UUID id = UUID.randomUUID();
        AccountJpaEntity entity = new AccountJpaEntity(
                id,
                "Test Account",
                "USD",
                new BigDecimal("100.00"),
                AccountStatus.ACTIVE,
                LocalDateTime.now());

        accountRepository.save(entity);

        // When
        Optional<AccountJpaEntity> found = accountRepository.findByIdLocked(id);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
    }

    @Test
    void given_NewAccountEntity_When_Saved_Then_CanBeRetrieved() {
        UUID id = UUID.randomUUID();
        AccountJpaEntity entity = new AccountJpaEntity(
                id,
                "Saver",
                "USD",
                new BigDecimal("0.00"),
                AccountStatus.ACTIVE,
                LocalDateTime.now());

        accountRepository.save(entity);

        Optional<AccountJpaEntity> loaded = accountRepository.findById(id);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getName()).isEqualTo("Saver");
    }
}
