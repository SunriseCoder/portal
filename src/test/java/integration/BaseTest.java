package integration;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@ComponentScan("app")
@Ignore
@RunWith(SpringRunner.class)
@SpringBootConfiguration
@SpringBootTest
public class BaseTest {

    protected Map<String, String> createParameters(String... args) {
        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            parameters.put(args[i], args[i + 1]);
        }
        return parameters;
    }
}
