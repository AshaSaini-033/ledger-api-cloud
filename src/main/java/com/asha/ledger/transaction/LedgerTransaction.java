package com.asha.ledger.transaction;

import com.asha.ledger.account.Account;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_transactions")
public class LedgerTransaction {
    @Id
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "reference_id")
    private UUID referenceId;
    private String description;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerTransaction() {}
    public LedgerTransaction(Account account, String type, BigDecimal amount, UUID referenceId, String description) {
        this.id = UUID.randomUUID();
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.description = description;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getAccountId() { return account.getId(); }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public UUID getReferenceId() { return referenceId; }
    public Instant getCreatedAt() { return createdAt; }
}