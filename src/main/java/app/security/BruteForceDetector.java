package app.security;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.entity.IPBanEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.enums.Users;
import app.service.AuditService;
import app.service.UserService;
import app.service.admin.IPBanService;

@Component
public class BruteForceDetector {
    private static final Logger logger = LogManager.getLogger(BruteForceDetector.class);

    private static final int MAX_FAILS_BEFORE_WAIT = 10;
    private static final int WAIT_MULTIPLIER = 2;
    private static final int MAX_BRUTES_BEFORE_IPBAN = 1000;

    @Autowired
    private AuditService auditService;
    @Autowired
    private IPBanService ipBanService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    private Map<String, Status> ipStatuses;

    public BruteForceDetector() {
        ipStatuses = new HashMap<>();
    }

    public void checkShouldWait() {
        List<String> ips = securityService.getIps();
        LocalDateTime waitUntil = shouldWait(ips);
        if (waitUntil != null) {
            throw new ShouldWaitException("You should wait before next login attempt", waitUntil);
        }
    }

    public void logBruteAttempt() {
        List<String> ips = securityService.getIps();
        addBruteAttempt(ips);
    }

    public void logFail() {
        List<String> ips = securityService.getIps();
        addFail(ips);
    }

    private LocalDateTime shouldWait(List<String> ips) {
        for (String ip : ips) {
            LocalDateTime waitUntil = shouldWait(ip);
            if (waitUntil != null) {
                return waitUntil;
            }
        }
        return null;
    }

    private LocalDateTime shouldWait(String ip) {
        Status status = ipStatuses.get(ip);

        if (status == null) {
            return null;
        }

        boolean shouldWait = LocalDateTime.now().isBefore(status.nextAttempt);
        return shouldWait ? status.nextAttempt : null;
    }

    private void addBruteAttempt(List<String> ips) {
        for (String ip: ips) {
            addBruteAttempt(ip);
        }
    }

    private void addBruteAttempt(String ip) {
        Status status = ipStatuses.get(ip);
        if (status == null) {
            status = new Status();
            status.bruteAttempt = 1;
            ipStatuses.put(ip, status);
        } else {
            status.bruteAttempt++;
        }

        if (status.bruteAttempt > MAX_BRUTES_BEFORE_IPBAN) {
            banIP(ip);
        }
    }

    private void addFail(List<String> ips) {
        for (String ip : ips) {
            addFail(ip);
        }
    }

    private void addFail(String ip) {
        Status status = ipStatuses.get(ip);
        if (status == null) {
            status = new Status();
            status.fails = 1;
            ipStatuses.put(ip, status);
        } else {
            status.fails++;
        }

        if (status.fails > MAX_FAILS_BEFORE_WAIT) {
            status.level++;
            LocalDateTime nextAttempt = calculateWaitDelay(status.level);
            status.nextAttempt = nextAttempt;
            status.nextReduction = nextAttempt;
        }
    }

    private LocalDateTime calculateWaitDelay(int level) {
        long delay = BigInteger.valueOf(WAIT_MULTIPLIER).pow(level).longValue();
        LocalDateTime nextAttempt = LocalDateTime.now().plusMinutes(delay);
        return nextAttempt;
    }

    private void banIP(String ip) {
        IPBanEntity entity = new IPBanEntity();
        entity.setIp(ip);
        entity.setReason("Password bruteforce");
        entity.setBannedBy(userService.findByLogin(Users.system.name()));
        try {
            ipBanService.add(entity);
            auditService.log(OperationTypes.ADMIN_IP_BAN, AuditEventTypes.SUSPICIOUS_ACTIVITY, ip, entity.getReason());
            ipStatuses.remove(ip);
        } catch (Exception e) {
            auditService.log(OperationTypes.ADMIN_IP_BAN, AuditEventTypes.SUSPICIOUS_ACTIVITY, ip, entity.getReason(), "Failed to ban IP");
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    private void reduction() {
        logger.info("Reduction job start");

        if (ipStatuses.isEmpty()) {
            return;
        }

        Iterator<Entry<String, Status>> iterator = ipStatuses.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Status> entry = iterator.next();
            Status status = entry.getValue();
            boolean completed = processReduction(status);
            if (completed) {
                iterator.remove();
            } else {

            }
        }
    }

    private boolean processReduction(Status status) {
        // Not yet ready for the reduction
        if (LocalDateTime.now().isBefore(status.nextReduction)) {
            return false;
        }

        boolean reduced = false;

        // If there are some delays, reducing them
        if (status.level > 0) {
            status.level--;
            LocalDateTime nextAttempt = calculateWaitDelay(status.level);
            status.nextAttempt = nextAttempt;
            status.nextReduction = nextAttempt;
            reduced = true;;
        }

        if (!reduced && status.fails > 0) {
            status.fails--;
            reduced = true;
        }

        // If there are some brute attempts, first reducing them
        if (!reduced && status.bruteAttempt > 0) {
            status.bruteAttempt--;
        }

        // Return true if the status has been completely reduced
        return status.level == 0 && status.fails == 0 && status.bruteAttempt == 0;
    }

    private class Status {
        public Status() {
            nextAttempt = LocalDateTime.now();
            nextReduction = LocalDateTime.now();
        }
        private int fails;
        private int bruteAttempt;
        private int level;
        private LocalDateTime nextAttempt;
        private LocalDateTime nextReduction;

        @Override
        public String toString() {
            return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
        }
    }
}
