package com.facture.system.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.facture.system.model.*;

public final class Dtos {
    private Dtos() {}

    public record LoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 72) String password
    ) {}
    public record TokenResponse(String token, long expiresIn) {}
    public record ClientRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 254) String email
    ) {}
    public record InvoiceRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull LocalDate date,
        @NotBlank @Size(max = 1000) String description
    ) {}
    public record ClientResponse(Long id, String name, String email) {
        public static ClientResponse from(Client client) {
            return new ClientResponse(client.getId(), client.getName(), client.getEmail());
        }
    }
    public record InvoiceResponse(Long id, BigDecimal amount, LocalDate date, String description) {
        public static InvoiceResponse from(Invoice invoice) {
            return new InvoiceResponse(invoice.getId(), invoice.getAmount(),
                invoice.getDate(), invoice.getDescription());
        }
    }
}
