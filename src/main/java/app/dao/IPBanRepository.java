package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.IPBanEntity;

public interface IPBanRepository extends JpaRepository<IPBanEntity, Long> {
    IPBanEntity findByIp(String ip);
}
