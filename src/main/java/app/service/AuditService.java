package app.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import app.entity.AuditEventEntity;
import app.entity.AuditEventTypeEntity;
import app.entity.OperationTypeEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;

@Service
public interface AuditService {
    List<AuditEventEntity> findEvents(Map<String, String> parameters);

    List<OperationTypeEntity> findAllOperationTypes();
    List<AuditEventTypeEntity> findAllEventTypes();

    void log(OperationTypes operation, AuditEventTypes eventType);
    void log(OperationTypes operation, AuditEventTypes eventType, String object);
    void log(OperationTypes operation, AuditEventTypes eventType, String objectBefore, String objectAfter);
    void log(OperationTypes operation, AuditEventTypes eventType, String objectBefore, String objectAfter, String error);
}
