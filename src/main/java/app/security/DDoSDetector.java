package app.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.service.UserService;
import app.service.admin.IPBanService;

@Component
public class DDoSDetector {
    private static final Logger logger = LogManager.getLogger(DDoSDetector.class.getName());

    private static final String IPBAN_REASON = "Requests quota exceed (DDoS)";

    @Value("${security.ddos.max-hourly-requests-before-ipban}")
    private int maxHourlyRequestsBeforeIPBan;
    @Value("${security.ddos.max-daily-requests-before-ipban}")
    private int maxDailyRequestsBeforeIPBan;

    @Autowired
    private IPBanService ipBanService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    private Map<String, Integer> hourlyRequestCounters;
    private Map<String, Integer> dailyRequestCounters;

    public DDoSDetector() {
        hourlyRequestCounters = new HashMap<>();
        dailyRequestCounters = new HashMap<>();
    }

    public void checkDDoS() throws RequestLimitException {
        List<String> ips = securityService.getIps();
        boolean safe = increaseAndCheck(ips);
        if (!safe) {
            banIPs(ips);
            throw new RequestLimitException(IPBAN_REASON);
        }
    }

    private void banIPs(List<String> ips) {
        for (String ip : ips) {
            banIP(ip);
            hourlyRequestCounters.remove(ip);
            dailyRequestCounters.remove(ip);
        }
    }

    private boolean increaseAndCheck(List<String> ips) {
        for (String ip : ips) {
            boolean safe = increaseAndCheck(ip);
            if (!safe) {
                return false;
            }
        }
        return true;
    }

    private boolean increaseAndCheck(String ip) {
        int hourly = createOrIncreaseAndGet(hourlyRequestCounters, ip);
        int daily = createOrIncreaseAndGet(dailyRequestCounters, ip);

        if (hourly > maxHourlyRequestsBeforeIPBan) {
            banIP(ip);
            hourlyRequestCounters.remove(ip);
            return false;
        }

        if (daily > maxDailyRequestsBeforeIPBan) {
            banIP(ip);
            dailyRequestCounters.remove(ip);
            return false;
        }

        return true;
    }

    private void banIP(String ip) {
        ipBanService.banIP(ip, IPBAN_REASON, userService.getSystemUser());
    }

    private Integer createOrIncreaseAndGet(Map<String, Integer> map, String key) {
        Integer value = map.get(key);
        if (value == null) {
            value = 0;
        }
        value++;
        map.put(key, value);
        return value;
    }

    @Scheduled(cron = "0 0 * * * *") // Hourly
    private void hourlyCleanup() {
        Optional<Integer> maxHits = hourlyRequestCounters.values().stream().max((a, b) -> a - b);
        logger.info("Hourly cleanup: max hits was: {}", maxHits.isPresent() ? maxHits.get() : 0);
        hourlyRequestCounters.clear();
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily
    private void dailyCleanup() {
        Optional<Integer> maxHits = dailyRequestCounters.values().stream().max((a, b) -> a - b);
        logger.info("Daily cleanup: max hits was: {}", maxHits.isPresent() ? maxHits.get() : 0);
        hourlyRequestCounters.clear();
    }
}
