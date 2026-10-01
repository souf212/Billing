package com.facture.system.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "invoices", indexes = @Index(name = "idx_invoice_client", columnList = "client_id"))
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false, length = 1000)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public Invoice(BigDecimal amount, LocalDate date, String description, Client client) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.client = client;
    }
}
