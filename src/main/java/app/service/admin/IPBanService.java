package app.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import app.entity.IPBanEntity;

@Service
public interface IPBanService {
    List<IPBanEntity> findAll();
    IPBanEntity findByIp(String ip);
    boolean isIPBanned();
    IPBanEntity add(IPBanEntity entity);
    IPBanEntity remove(Long id);
}
