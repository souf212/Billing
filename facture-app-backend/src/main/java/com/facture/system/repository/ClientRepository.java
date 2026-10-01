package com.facture.system.repository;

import com.facture.system.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findAllByAdminIdOrderByNameAsc(Long adminId);
    Optional<Client> findByIdAndAdminId(Long id, Long adminId);
}
