package com.mahmoud.ledger.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface AccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
}

interface TransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
}
