package com.asha.ledger.auth;

import java.util.UUID;

public record CurrentUser(UUID id, String email) {}