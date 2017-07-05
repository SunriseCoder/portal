package integration;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import app.dao.OperationTypeRepository;
import app.entity.OperationTypeEntity;
import app.enums.OperationTypes;

public class OperationTypesTest extends BaseTest {
    @Autowired
    private OperationTypeRepository operationTypeRepository;

    @Test
    public void testOperationTypeIntegrity() {
        List<OperationTypeEntity> fromDB = operationTypeRepository.findAll();
        OperationTypes[] fromEnum = OperationTypes.values();

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
}
