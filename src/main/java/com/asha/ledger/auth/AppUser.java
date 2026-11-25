package com.asha.ledger.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AppUser() {}
    public AppUser(String email, String passwordHash) {
        this.id = UUID.randomUUID();
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
}