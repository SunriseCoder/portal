package app.service.admin;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import app.dao.IPBanRepository;
import app.entity.IPBanEntity;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.security.SecurityService;
import app.service.AuditService;
import app.service.UserService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class IPBanServiceImpl implements IPBanService {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(IPBanServiceImpl.class.getName());

    @Autowired
    private AuditService auditService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    @Autowired
    private IPBanRepository repository;

    private Set<String> bannedIPs;
    private Date lastUpdate;

    @PostConstruct
    public void updateCache() {
        Set<String> set = new HashSet<>();
        lastUpdate = new Date();
        List<IPBanEntity> list = repository.findAll();
        for (IPBanEntity entity : list) {
            set.add(entity.getIp());
        }

        bannedIPs = set;
    }

    @Override
    public List<IPBanEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public IPBanEntity findByIp(String ip) {
        return repository.findByIp(ip);
    }

    @Override
    public boolean isIPBanned() {
        List<String> ips = securityService.getIps();
        for (String ip : ips) {
            boolean isBanned = bannedIPs.contains(ip);
            if (isBanned) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public IPBanEntity add(IPBanEntity entity) {
        entity.setDate(new Date());
        if (entity.getBannedBy() == null) {
            entity.setBannedBy(userService.getLoggedInUser());
        }
        entity = repository.save(entity);
        bannedIPs.add(entity.getIp());
        lastUpdate = new Date();
        return entity;
    }

    @Override
    @Transactional
    public IPBanEntity remove(Long id) {
        IPBanEntity entity = repository.findOne(id);
        repository.delete(entity);
        bannedIPs.remove(entity.getIp());
        lastUpdate = new Date();
        return entity;
    }

    public long getSize() {
        return bannedIPs.size();
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public void banIP(String ip, String reason, UserEntity bannedBy) {
        if (bannedIPs.contains(ip)) {
            return;
        }

        IPBanEntity entity = new IPBanEntity();
        entity.setIp(ip);
        entity.setReason(reason);
        entity.setBannedBy(bannedBy);
        try {
            add(entity);
            auditService.log(OperationTypes.ADMIN_IP_BAN, AuditEventTypes.SUSPICIOUS_ACTIVITY, ip, entity.getReason());
        } catch (Exception e) {
            auditService.log(OperationTypes.ADMIN_IP_BAN, AuditEventTypes.SUSPICIOUS_ACTIVITY, ip, entity.getReason(), "Failed to ban IP");
        }
    }
}
