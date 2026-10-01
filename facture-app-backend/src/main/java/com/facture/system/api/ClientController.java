package com.facture.system.api;

import com.facture.system.service.BillingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.facture.system.api.Dtos.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final BillingService billing;

    public ClientController(BillingService billing) {
        this.billing = billing;
    }

    private Long adminId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping
    public List<ClientResponse> clients(@AuthenticationPrincipal Jwt jwt) {
        return billing.clients(adminId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse addClient(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ClientRequest request) {
        return billing.addClient(adminId(jwt), request);
    }

    @GetMapping("/{clientId}/invoices")
    public List<InvoiceResponse> invoices(@AuthenticationPrincipal Jwt jwt, @PathVariable Long clientId) {
        return billing.invoices(adminId(jwt), clientId);
    }

    @PostMapping("/{clientId}/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse addInvoice(@AuthenticationPrincipal Jwt jwt, @PathVariable Long clientId,
                                     @Valid @RequestBody InvoiceRequest request) {
        return billing.addInvoice(adminId(jwt), clientId, request);
    }
}
