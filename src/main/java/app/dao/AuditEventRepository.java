package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import app.entity.AuditEventEntity;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long>, JpaSpecificationExecutor<AuditEventEntity> {

}
