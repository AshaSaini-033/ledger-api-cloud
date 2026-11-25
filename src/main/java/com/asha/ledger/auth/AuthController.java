package com.asha.ledger.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record Credentials(@Email @NotBlank String email,
                              @NotBlank @Size(min = 8, max = 72) String password) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody Credentials request) {
        String email = request.email().toLowerCase().trim();
        if (users.existsByEmail(email)) throw new IllegalArgumentException("Email is already registered");
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password())));
        return Map.of("token", jwtService.issue(user.getEmail()), "tokenType", "Bearer",
                "userId", user.getId().toString(), "email", user.getEmail());
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody Credentials request) {
        AppUser user = users.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return Map.of("token", jwtService.issue(user.getEmail()), "tokenType", "Bearer",
                "userId", user.getId().toString(), "email", user.getEmail());
    }
}