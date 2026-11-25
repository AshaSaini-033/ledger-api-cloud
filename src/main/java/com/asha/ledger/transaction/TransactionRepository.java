package com.asha.ledger.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<LedgerTransaction, UUID> {
    List<LedgerTransaction> findTop100ByAccountIdOrderByCreatedAtDesc(UUID accountId);
}