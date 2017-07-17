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
import app.service.UserService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class IPBanServiceImpl implements IPBanService {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(IPBanServiceImpl.class.getName());

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
    public boolean isBanned(String ip) {
        boolean isBanned = bannedIPs.contains(ip);
        return isBanned;
    }

    @Override
    @Transactional
    public IPBanEntity add(IPBanEntity entity) {
        entity.setDate(new Date());
        entity.setBannedBy(userService.getLoggedInUser());
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
}
