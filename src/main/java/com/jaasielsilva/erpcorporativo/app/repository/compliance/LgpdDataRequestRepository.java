package com.jaasielsilva.erpcorporativo.app.repository.compliance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.LgpdDataRequest;
import com.jaasielsilva.erpcorporativo.app.model.LgpdRequestStatus;

public interface LgpdDataRequestRepository extends JpaRepository<LgpdDataRequest, Long> {

    List<LgpdDataRequest> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<LgpdDataRequest> findAllByStatusOrderByCreatedAtDesc(LgpdRequestStatus status);
}
