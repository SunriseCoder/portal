package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.AuditEventEntity;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

}
