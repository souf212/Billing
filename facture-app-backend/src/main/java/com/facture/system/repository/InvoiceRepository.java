package com.facture.system.repository;

import com.facture.system.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAllByClientIdAndClientAdminIdOrderByDateDescIdDesc(Long clientId, Long adminId);
}
