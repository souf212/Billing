package com.facture.system.config;

import com.facture.system.model.*;
import com.facture.system.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoData implements CommandLineRunner {
    private final AdminRepository admins;
    private final ClientRepository clients;
    private final InvoiceRepository invoices;
    private final PasswordEncoder passwords;

    public DemoData(AdminRepository admins, ClientRepository clients, InvoiceRepository invoices,
                    PasswordEncoder passwords) {
        this.admins = admins;
        this.clients = clients;
        this.invoices = invoices;
        this.passwords = passwords;
    }

    @Override
    @Transactional
    public void run(String... args) {
        var alice = admins.save(new Admin("alice@example.com", passwords.encode("Alice-demo-2026!")));
        var bob = admins.save(new Admin("bob@example.com", passwords.encode("Bob-demo-2026!")));
        var clientAlice = clients.save(new Client("Atelier Atlas", "contact@atlas.example", alice));
        var clientBob = clients.save(new Client("Studio Horizon", "contact@horizon.example", bob));
        invoices.save(new Invoice(new BigDecimal("1200.00"), LocalDate.now(), "Prestation de conseil", clientAlice));
        invoices.save(new Invoice(new BigDecimal("850.00"), LocalDate.now(), "Création graphique", clientBob));
    }
}
