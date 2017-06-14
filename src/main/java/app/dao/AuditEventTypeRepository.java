package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.AuditEventTypeEntity;

public interface AuditEventTypeRepository extends JpaRepository<AuditEventTypeEntity, Long> {
    AuditEventTypeEntity findByName(String name);
}
