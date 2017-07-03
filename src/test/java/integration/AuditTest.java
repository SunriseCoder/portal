package integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import app.dao.AuditEventRepository;
import app.dao.AuditEventTypeRepository;
import app.dao.OperationTypeRepository;
import app.dao.UserRepository;
import app.entity.AuditEventEntity;
import app.entity.AuditEventTypeEntity;
import app.entity.OperationTypeEntity;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.AuditService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class AuditTest {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;
    @Autowired
    private AuditEventTypeRepository auditEventTypeRepository;
    @Autowired
    private OperationTypeRepository operationTypeRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testAuditEventTypes() {
        // Checking total amount
        List<AuditEventTypeEntity> fromDB = auditEventTypeRepository.findAll();
        AuditEventTypes[] fromEnum = AuditEventTypes.values();
        assertEquals("Amount of elements in database is different from enum size", fromEnum.length, fromDB.size());

        // Checking necessary elements in DB
        Set<String> dbSet = fromDB.stream().map(e -> e.getName()).collect(Collectors.toSet());
        for (AuditEventTypes t : fromEnum) {
            assertTrue("Element '" + t.name() + "' is not found in database", dbSet.contains(t.name()));
        }

        // Checking useless elements in DB
        Set<String> enumSet = Arrays.stream(fromEnum).map(e -> e.name()).collect(Collectors.toSet());
        for (AuditEventTypeEntity t : fromDB) {
            assertTrue("Element '" + t.getName() + "' found in database, but never used", enumSet.contains(t.getName()));
        }
    }

    @Test
    public void testOperationTypes() {
        // Checking total amount
        List<OperationTypeEntity> fromDB = operationTypeRepository.findAll();
        OperationTypes[] fromEnum = OperationTypes.values();
        assertEquals("Amount of elements in database is different from enum size", fromEnum.length, fromDB.size());

        // Checking necessary elements in DB
        Set<String> dbSet = fromDB.stream().map(e -> e.getName()).collect(Collectors.toSet());
        for (OperationTypes t : fromEnum) {
            assertTrue("Element '" + t.name() + "' is not found in database", dbSet.contains(t.name()));
        }

        // Checking useless elements in DB
        Set<String> enumSet = Arrays.stream(fromEnum).map(e -> e.name()).collect(Collectors.toSet());
        for (OperationTypeEntity t : fromDB) {
            assertTrue("Element '" + t.getName() + "' found in database, but never used", enumSet.contains(t.getName()));
        }
    }

    @Test
    public void testAuditFilters() throws Exception {
        // Setup
        List<AuditEventEntity> allEvents = new ArrayList<>();
        UserEntity user1 = userRepository.findByLogin("admin");
        UserEntity user2 = userRepository.findByLogin("moder");
        allEvents.add(createEvent(user1, "2017-01-01", "192.168.0.1", OperationTypes.ACCESS_PAGE_MAIN, AuditEventTypes.ACCESS_ALLOWED));
        allEvents.add(createEvent(user1, "2017-01-02", "192.168.0.1", OperationTypes.ACCESS_PAGE_MAIN, AuditEventTypes.ACCESS_ALLOWED));
        allEvents.add(createEvent(user2, "2017-01-03", "0:0:0:0:0:0:0:1", OperationTypes.ACCESS_ADMIN_AUDIT, AuditEventTypes.ACCESS_DENIED));

        auditEventRepository.save(allEvents);

        // Check no filter
        List<AuditEventEntity> events = auditService.findEvents(createParameters());
        assertEquals(0, events.size()); // By default filter gets only events from today

        // Check filter "from"
        events = auditService.findEvents(createParameters("from", "2017-01-02"));
        assertEquals(2, events.size());

        // Check filter "to"
        events = auditService.findEvents(createParameters("from", "2017-01-02", "to", "2017-01-02"));
        assertEquals(1, events.size());

        // Check filter "user"
        events = auditService.findEvents(createParameters("from", "2017-01-01", "to", "2017-01-05", "user", user1.getLogin()));
        assertEquals(2, events.size());

        // Check filter "ip"
        events = auditService.findEvents(createParameters("from", "2017-01-01", "to", "2017-01-05", "ip", "192.168.0.1"));
        assertEquals(2, events.size());

        // Check filter "operation"
        events = auditService.findEvents(createParameters("from", "2017-01-01", "to", "2017-01-05",
                        "operation", getOperationTypeId(OperationTypes.ACCESS_ADMIN_AUDIT)));
        assertEquals(1, events.size());

        // Check filter "eventType"
        events = auditService.findEvents(createParameters("from", "2017-01-01", "to", "2017-01-05",
                        "type", getEventTypeId(AuditEventTypes.ACCESS_DENIED)));
        assertEquals(1, events.size());

        // Tear down
        auditEventRepository.delete(allEvents);

    }

    private AuditEventEntity createEvent(UserEntity user, String dateStr, String ip,
                    OperationTypes operationType, AuditEventTypes eventType) throws Exception {
        AuditEventEntity event = new AuditEventEntity();
        event.setUser(user);
        event.setDate(DATE_FORMAT.parse(dateStr));
        event.setIp(ip);
        event.setOperation(operationTypeRepository.findByName(operationType.name()));
        event.setType(auditEventTypeRepository.findByName(eventType.name()));
        return event;
    }

    private Map<String, String> createParameters(String... args) {
        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            parameters.put(args[i], args[i + 1]);
        }
        return parameters;
    }

    private String getOperationTypeId(OperationTypes operationType) {
        OperationTypeEntity entity = operationTypeRepository.findByName(operationType.name());
        return entity.getId().toString();
    }

    private String getEventTypeId(AuditEventTypes eventType) {
        AuditEventTypeEntity entity = auditEventTypeRepository.findByName(eventType.name());
        return entity.getId().toString();
    }
}
