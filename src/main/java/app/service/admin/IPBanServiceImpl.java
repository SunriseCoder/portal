package app.service.admin;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.IPBanRepository;
import app.entity.IPBanEntity;
import app.service.UserService;

@Component
public class IPBanServiceImpl implements IPBanService {
    @Autowired
    private UserService userService;

    @Autowired
    private IPBanRepository repository;

    private Set<String> bannedIPs;

    @PostConstruct
    private void loadData() {
        bannedIPs = new HashSet<>();
        List<IPBanEntity> list = repository.findAll();
        for (IPBanEntity entity : list) {
            bannedIPs.add(entity.getIp());
        }
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
        return entity;
    }

    @Override
    @Transactional
    public IPBanEntity remove(Long id) {
        IPBanEntity entity = repository.findOne(id);
        repository.delete(entity);
        bannedIPs.remove(entity.getIp());
        return entity;
    }
}
