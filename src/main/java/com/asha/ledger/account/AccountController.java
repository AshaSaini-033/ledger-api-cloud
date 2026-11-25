package com.asha.ledger.account;

import com.asha.ledger.auth.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accounts;
    private final com.asha.ledger.auth.UserRepository users;

    public AccountController(AccountRepository accounts, com.asha.ledger.auth.UserRepository users) {
        this.accounts = accounts;
        this.users = users;
    }

    public record CreateAccountRequest(@NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency) {}
    public record AccountResponse(UUID id, String currency, BigDecimal balance, String status, Instant createdAt) {
        static AccountResponse from(Account account) {
            return new AccountResponse(account.getId(), account.getCurrency(), account.getBalance(),
                    account.getStatus(), account.getCreatedAt());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@AuthenticationPrincipal CurrentUser user,
                                  @Valid @RequestBody CreateAccountRequest request) {
        var owner = users.findById(user.id()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return AccountResponse.from(accounts.save(new Account(owner, request.currency())));
    }

    @GetMapping
    public List<AccountResponse> list(@AuthenticationPrincipal CurrentUser user) {
        return accounts.findByOwnerIdOrderByCreatedAtDesc(user.id()).stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/{accountId}")
    public AccountResponse get(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID accountId) {
        return AccountResponse.from(owned(user.id(), accountId));
    }

    @PostMapping("/{accountId}/close")
    @Transactional
    public Map<String, String> close(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID accountId) {
        Account account = owned(user.id(), accountId);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) throw new IllegalStateException("Account balance must be zero before closing");
        account.close();
        accounts.save(account);
        return Map.of("status", "CLOSED");
    }

    private Account owned(UUID userId, UUID accountId) {
        return accounts.findByIdAndOwnerId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }
}