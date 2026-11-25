package com.asha.ledger.transaction;

import com.asha.ledger.account.Account;
import com.asha.ledger.account.AccountRepository;
import com.asha.ledger.auth.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts/{accountId}")
public class TransactionController {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public TransactionController(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public record AmountRequest(@NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
                                String description) {}
    public record TransactionResponse(UUID id, String type, BigDecimal amount, UUID referenceId,
                                       java.time.Instant createdAt) {
        static TransactionResponse from(LedgerTransaction tx) {
            return new TransactionResponse(tx.getId(), tx.getType(), tx.getAmount(), tx.getReferenceId(), tx.getCreatedAt());
        }
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public TransactionResponse deposit(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID accountId,
                                       @Valid @RequestBody AmountRequest request) {
        Account account = owned(user.id(), accountId);
        account.credit(request.amount());
        return TransactionResponse.from(transactions.save(new LedgerTransaction(account, "DEPOSIT",
                request.amount(), null, request.description())));
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public TransactionResponse withdraw(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID accountId,
                                        @Valid @RequestBody AmountRequest request) {
        Account account = owned(user.id(), accountId);
        account.debit(request.amount());
        return TransactionResponse.from(transactions.save(new LedgerTransaction(account, "WITHDRAWAL",
                request.amount(), null, request.description())));
    }

    @GetMapping("/transactions")
    @Transactional(readOnly = true)
    public List<TransactionResponse> history(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID accountId) {
        owned(user.id(), accountId);
        return transactions.findTop100ByAccount_IdOrderByCreatedAtDesc(accountId).stream()
                .map(TransactionResponse::from).toList();
    }

    private Account owned(UUID userId, UUID accountId) {
        return accounts.findByIdAndOwnerId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }
}