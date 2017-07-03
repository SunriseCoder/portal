package app.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
import app.util.DateUtils;

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
    private AuditEventTypeRepository eventTypeRepository;

    @Override
    public List<AuditEventEntity> findEvents(Map<String, String> parameters) {
        String user = parameters.get("user");
        String ip = parameters.get("ip");
        String operation = parameters.get("operation");
        String type = parameters.get("type");

        // TODO Rewrite it and test to use ZonedDateTime if JPA 2.2 will support it
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
        Date today = DateUtils.parseDateSilent(dateFormat, dateFormat.format(new Date()));

        Date rawFrom = DateUtils.parseDateSilent(dateFormat, parameters.get("from"));
        rawFrom = rawFrom == null ? today : rawFrom;

        Date rawTo = DateUtils.parseDateSilent(dateFormat, parameters.get("to"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(rawTo == null ? today : rawTo);
        // Increasing filter parameter "to" by 1 day to get strict interval condition
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        rawTo = calendar.getTime();

        Date from = rawFrom;
        Date to = rawTo;

        Specification<AuditEventEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (from != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (to != null) {
                p = cb.and(p, cb.lessThan(root.get("date"), to));
            }
            if (user != null && !user.isEmpty()) {
                if ("<null>".equals(user)) { // User is null, i.e. anonymous user
                    p = cb.and(p, cb.isNull(root.get("user")));
                } else {
                    p = cb.and(p, cb.like(root.get("user").get("login"), user));
                }
            }
            if (ip != null && !ip.isEmpty()) {
                p = cb.and(p, cb.like(root.get("ip"), ip));
            }
            if (operation != null && !operation.isEmpty() && !"0".equals(operation)) {
                p = cb.and(p, cb.equal(root.get("operation").get("id"), operation));
            }
            if (type != null && !type.isEmpty() && !"0".equals(type)) {
                p = cb.and(p, cb.equal(root.get("type").get("id"), type));
            }
            return p;
        };

        return auditRepository.findAll(spec);
    }

    @Override
    public List<OperationTypeEntity> findAllOperationTypes() {
        return operationRepository.findAll();
    }

    @Override
    public List<AuditEventTypeEntity> findAllEventTypes() {
        return eventTypeRepository.findAll();
    }

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
        AuditEventTypeEntity entity = eventTypeRepository.findByName(eventType.name());
        if (entity == null) {
            log(OperationTypes.SAVE_AUDIT_EVENT, AuditEventTypes.AUDIT_FAILURE, null, null, "AuditEventType '" + eventType + "' was not found");
        }
        return entity;
    }
}
