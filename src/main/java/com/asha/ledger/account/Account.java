package com.asha.ledger.account;

import com.asha.ledger.auth.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private AppUser owner;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false)
    private String status = "ACTIVE";
    @Version
    private long version;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Account() {}
    public Account(AppUser owner, String currency) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.currency = currency.toUpperCase();
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public AppUser getOwner() { return owner; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public String getStatus() { return status; }
    public void credit(BigDecimal amount) { ensureActive(); balance = balance.add(amount); }
    public void debit(BigDecimal amount) {
        ensureActive();
        if (balance.compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds");
        balance = balance.subtract(amount);
    }
    private void ensureActive() {
        if (!"ACTIVE".equals(status)) throw new IllegalStateException("Account is not active");
    }
}