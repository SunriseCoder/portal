package app.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

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
            auditRepository.save(event);
        }
    }

    private OperationTypeEntity getOperation(OperationTypes operation) {
        return operationRepository.findByName(operation.name());
    }

    private AuditEventTypeEntity getAuditEventType(AuditEventTypes eventType) {
        return auditEventTypeRepository.findByName(eventType.name());
    }
}
