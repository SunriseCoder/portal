package integration;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import app.security.AccessRule;
import app.security.SecurityChecker;

public class URLMappingsTest extends BaseTest {
    @Autowired
    private RequestMappingHandlerMapping mapping;
    @Autowired
    private SecurityChecker securityChecker;

    @Test
    public void testChecksForAllMappings() {
        Map<String, AccessRule> urlRules = securityChecker.getUrlRules();

        Map<RequestMappingInfo, HandlerMethod> methods = mapping.getHandlerMethods();
        for (Entry<RequestMappingInfo, HandlerMethod> method : methods.entrySet()) {
            Set<String> patterns = method.getKey().getPatternsCondition().getPatterns();

            for (String pattern : patterns) {
                String handler = method.getValue().getMethod().getDeclaringClass().getName() + "."
                                + method.getValue().getMethod().getName() + "(...)";
                assertTrue("Pattern '" + pattern + "' has no security rules in '" + SecurityChecker.class.getName()
                                + "' (URL Mapping is in '" + handler + "')", urlRules.containsKey(pattern));
            }
        }
    }


    @Test
    public void testFindGhostChecks() {
        Map<String, AccessRule> urlRules = securityChecker.getUrlRules();

        Set<String> patternSet = new HashSet<>();
        Map<RequestMappingInfo, HandlerMethod> methods = mapping.getHandlerMethods();
        for (Entry<RequestMappingInfo, HandlerMethod> method : methods.entrySet()) {
            Set<String> patterns = method.getKey().getPatternsCondition().getPatterns();
            for (String pattern : patterns) {
                patternSet.add(pattern);
            }
        }

        for (String pattern : urlRules.keySet()) {
            assertTrue("Security Check is redundant or endpoint is not implemented yet: " + pattern, patternSet.contains(pattern));
        }
    }
}
