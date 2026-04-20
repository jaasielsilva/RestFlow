package com.jaasielsilva.erpcorporativo.app.repository.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jaasielsilva.erpcorporativo.app.model.NotificationAudience;
import com.jaasielsilva.erpcorporativo.app.model.PlatformNotification;

public interface PlatformNotificationRepository extends JpaRepository<PlatformNotification, Long> {

    boolean existsBySourceKey(String sourceKey);

    long countByAudienceAndReadAtIsNull(NotificationAudience audience);

    long countByAudienceAndTenantIdAndReadAtIsNull(NotificationAudience audience, Long tenantId);

    List<PlatformNotification> findTop10ByAudienceOrderByCreatedAtDesc(NotificationAudience audience);

    List<PlatformNotification> findTop10ByAudienceAndTenantIdOrderByCreatedAtDesc(NotificationAudience audience, Long tenantId);

    List<PlatformNotification> findTop200ByAudienceOrderByCreatedAtDesc(NotificationAudience audience);

    List<PlatformNotification> findTop200ByAudienceAndTenantIdOrderByCreatedAtDesc(NotificationAudience audience, Long tenantId);

    @Modifying
    @Query("""
            update PlatformNotification n
               set n.readAt = :readAt
             where n.audience = :audience
               and n.readAt is null
            """)
    int markAllAsRead(NotificationAudience audience, LocalDateTime readAt);

    @Modifying
    @Query("""
            update PlatformNotification n
               set n.readAt = :readAt
             where n.audience = :audience
               and n.tenantId = :tenantId
               and n.readAt is null
            """)
    int markAllAsReadByTenant(NotificationAudience audience, Long tenantId, LocalDateTime readAt);
}
