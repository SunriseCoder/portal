package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import app.entity.AuditEventEntity;
import app.entity.StatisticLogEntity;

public interface StatisticLogRepository extends JpaRepository<StatisticLogEntity, Long>, JpaSpecificationExecutor<AuditEventEntity> {

}
