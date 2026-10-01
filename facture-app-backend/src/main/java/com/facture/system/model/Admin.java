package com.facture.system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false)
    private String password;

    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
