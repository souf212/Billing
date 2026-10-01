package com.facture.system.service;

import com.facture.system.model.*;
import com.facture.system.repository.*;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static com.facture.system.api.Dtos.*;

@Service
@Transactional(readOnly = true)
public class BillingService {
    private final AdminRepository admins;
    private final ClientRepository clients;
    private final InvoiceRepository invoices;

    public BillingService(AdminRepository admins, ClientRepository clients, InvoiceRepository invoices) {
        this.admins = admins;
        this.clients = clients;
        this.invoices = invoices;
    }

    public List<ClientResponse> clients(Long adminId) {
        return clients.findAllByAdminIdOrderByNameAsc(adminId).stream().map(ClientResponse::from).toList();
    }

    @Transactional
    public ClientResponse addClient(Long adminId, ClientRequest request) {
        Admin admin = admins.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return ClientResponse.from(clients.save(new Client(request.name().trim(),
            request.email().trim().toLowerCase(Locale.ROOT), admin)));
    }

    private Client ownedClient(Long adminId, Long clientId) {
        // Same 404 for an unknown client and a client belonging to another tenant.
        return clients.findByIdAndAdminId(clientId, adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<InvoiceResponse> invoices(Long adminId, Long clientId) {
        ownedClient(adminId, clientId);
        return invoices.findAllByClientIdAndClientAdminIdOrderByDateDescIdDesc(clientId, adminId)
            .stream().map(InvoiceResponse::from).toList();
    }

    @Transactional
    public InvoiceResponse addInvoice(Long adminId, Long clientId, InvoiceRequest request) {
        Client client = ownedClient(adminId, clientId);
        return InvoiceResponse.from(invoices.save(new Invoice(request.amount(), request.date(),
            request.description().trim(), client)));
    }
}
