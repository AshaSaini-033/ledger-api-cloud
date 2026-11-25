package com.asha.ledger.transfer;

import com.asha.ledger.account.Account;
import com.asha.ledger.account.AccountRepository;
import com.asha.ledger.auth.CurrentUser;
import com.asha.ledger.transaction.LedgerTransaction;
import com.asha.ledger.transaction.TransactionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public TransferController(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public record TransferRequest(@NotNull UUID fromAccountId, @NotNull UUID toAccountId,
                                  @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
                                  String description) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> transfer(@AuthenticationPrincipal CurrentUser user,
                                        @Valid @RequestBody TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        Account source = accounts.findByIdAndOwnerId(request.fromAccountId(), user.id())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account destination = accounts.findById(request.toAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new IllegalArgumentException("Account currencies must match");
        }

        source.debit(request.amount());
        destination.credit(request.amount());

        UUID transferId = UUID.randomUUID();
        LedgerTransaction debit = transactions.save(new LedgerTransaction(source, "TRANSFER_OUT",
                request.amount(), transferId, request.description()));
        LedgerTransaction credit = transactions.save(new LedgerTransaction(destination, "TRANSFER_IN",
                request.amount(), transferId, request.description()));

        return Map.of("transferId", transferId, "fromAccountId", source.getId(),
                "toAccountId", destination.getId(), "amount", request.amount(),
                "currency", source.getCurrency(), "debitTransactionId", debit.getId(),
                "creditTransactionId", credit.getId(), "status", "COMPLETED");
    }
}