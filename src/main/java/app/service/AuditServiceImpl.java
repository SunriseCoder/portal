package app.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.AuditEventRepository;
import app.dao.AuditEventTypeRepository;
import app.dao.OperationTypeRepository;
import app.entity.AuditEventEntity;
import app.entity.AuditEventTypeEntity;
import app.entity.OperationTypeEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.security.SecurityService;

@Component
public class AuditServiceImpl implements AuditService {
    private static final Logger logger = LogManager.getLogger(AuditServiceImpl.class.getName());

    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    @Autowired
    private AuditEventRepository auditRepository;
    @Autowired
    private OperationTypeRepository operationRepository;
    @Autowired
    private AuditEventTypeRepository auditEventTypeRepository;

    @Override
    public void log(OperationTypes operation, AuditEventTypes eventType) {
        log(operation, eventType, null);
    }

    @Override
    public void log(OperationTypes operation, AuditEventTypes eventType, String object) {
        log(operation, eventType, object, null);
    }

    @Override
    public void log(OperationTypes operation, AuditEventTypes eventType, String objectBefore, String objectAfter) {
        log(operation, eventType, objectBefore, objectAfter, null);
    }

    @Override
    public void log(OperationTypes operation, AuditEventTypes eventType, String objectBefore, String objectAfter, String error) {
        List<String> ips = securityService.getIps();
        for (String ip : ips) {
            AuditEventEntity event = new AuditEventEntity();
            event.setUser(userService.getLoggedInUser());
            event.setDate(new Date());
            event.setIp(ip);
            event.setOperation(getOperationTypeEntity(operation));
            event.setType(getAuditEventTypeEntity(eventType));
            event.setObjectBefore(objectBefore);
            event.setObjectAfter(objectAfter);
            event.setError(error);
            save(event);
        }
    }

    @Transactional
    private void save(AuditEventEntity event) {
        try {
            auditRepository.save(event);
        } catch (Exception e) {
            logger.error("Error due to save " + event, e);
            saveFailureEvent(event);
        }
    }

    private void saveFailureEvent(AuditEventEntity event) {
        try {
            AuditEventEntity failureEvent = new AuditEventEntity();
            failureEvent.setUser(event.getUser());
            failureEvent.setDate(event.getDate());
            failureEvent.setIp(event.getIp());
            failureEvent.setOperation(getOperationTypeEntity(OperationTypes.SAVE_AUDIT_EVENT));
            failureEvent.setType(getAuditEventTypeEntity(AuditEventTypes.AUDIT_FAILURE));
            failureEvent.setError("See log file for details");
            auditRepository.save(failureEvent);
        } catch (Exception e) {
            logger.error("Error due to save audit failure event", e);
        }
    }

    private OperationTypeEntity getOperationTypeEntity(OperationTypes operationType) {
        OperationTypeEntity entity = operationRepository.findByName(operationType.name());
        if (entity == null) {
            log(OperationTypes.SAVE_AUDIT_EVENT, AuditEventTypes.AUDIT_FAILURE, null, null, "OperationType '" + operationType + "' was not found");
        }
        return entity;
    }

    private AuditEventTypeEntity getAuditEventTypeEntity(AuditEventTypes eventType) {
        AuditEventTypeEntity entity = auditEventTypeRepository.findByName(eventType.name());
        if (entity == null) {
            log(OperationTypes.SAVE_AUDIT_EVENT, AuditEventTypes.AUDIT_FAILURE, null, null, "AuditEventType '" + eventType + "' was not found");
        }
        return entity;
    }
}
