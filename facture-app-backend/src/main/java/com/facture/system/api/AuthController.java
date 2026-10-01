package com.facture.system.api;

import com.facture.system.repository.AdminRepository;
import com.facture.system.security.SecurityConfig;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.facture.system.api.Dtos.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AdminRepository admins;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final String dummyHash;

    public AuthController(AdminRepository admins, PasswordEncoder passwords, JwtEncoder encoder) {
        this.admins = admins;
        this.passwords = passwords;
        this.encoder = encoder;
        this.dummyHash = passwords.encode("dummy-password-for-timing");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        var admin = admins.findByEmail(request.email().trim().toLowerCase(Locale.ROOT));
        boolean valid = passwords.matches(request.password(), admin.map(a -> a.getPassword()).orElse(dummyHash));
        if (!valid || admin.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
            .issuer(SecurityConfig.ISSUER)
            .subject(admin.orElseThrow().getId().toString())
            .issuedAt(now).expiresAt(now.plusSeconds(3600)).build();
        String token = encoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new TokenResponse(token, 3600));
    }
}
