package com.facture.system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients", indexes = @Index(name = "idx_client_admin", columnList = "admin_id"))
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 254)
    private String email;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    public Client(String name, String email, Admin admin) {
        this.name = name;
        this.email = email;
        this.admin = admin;
    }
}
