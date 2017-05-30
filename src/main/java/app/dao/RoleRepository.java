package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    RoleEntity findByName(String name);
}
