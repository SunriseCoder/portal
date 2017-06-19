package app.service;

import org.springframework.stereotype.Service;

import app.enums.AuditEventTypes;
import app.enums.OperationTypes;

@Service
public interface AuditService {
    void log(OperationTypes operation, AuditEventTypes eventType);
    void log(OperationTypes operation, AuditEventTypes eventType, String object);
    void log(OperationTypes operation, AuditEventTypes eventType, String objectBefore, String objectAfter);
    void log(OperationTypes operation, AuditEventTypes eventType, String objectBefore, String objectAfter, String error);
}
