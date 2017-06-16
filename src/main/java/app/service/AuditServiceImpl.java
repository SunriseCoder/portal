package app.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
    @Transactional
    public void log(OperationTypes operation, AuditEventTypes eventType) {
        List<String> ips = securityService.getIps();
        for (String ip : ips) {
            AuditEventEntity event = new AuditEventEntity();
            event.setUser(userService.getLoggedInUser());
            event.setDate(new Date());
            event.setIp(ip);
            event.setOperation(getOperation(operation));
            event.setType(getAuditEventType(eventType));
            save(event);
        }
    }

    private void save(AuditEventEntity event) {
        try {
            auditRepository.save(event);
        } catch (Exception e) {
            String object = ReflectionToStringBuilder.toString(event, ToStringStyle.SHORT_PREFIX_STYLE);
            logger.error("Error due to save " + object, e);
            saveFailureEvent(event);
        }
    }

    private void saveFailureEvent(AuditEventEntity event) {
        try {
            AuditEventEntity failureEvent = new AuditEventEntity();
            failureEvent.setUser(event.getUser());
            failureEvent.setDate(event.getDate());
            failureEvent.setIp(event.getIp());
            failureEvent.setOperation(getOperation(OperationTypes.SAVE_AUDIT_EVENT));
            failureEvent.setType(getAuditEventType(AuditEventTypes.AUDIT_FAILURE));
            failureEvent.setObjectBefore("See log file for details");
            auditRepository.save(failureEvent);
        } catch (Exception e) {
            logger.error("Error due to save audit failure event", e);
        }
    }

    private OperationTypeEntity getOperation(OperationTypes operation) {
        return operationRepository.findByName(operation.name());
    }

    private AuditEventTypeEntity getAuditEventType(AuditEventTypes eventType) {
        return auditEventTypeRepository.findByName(eventType.name());
    }
}
