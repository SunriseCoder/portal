package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.RoleEntity;
import java.lang.String;
import java.util.List;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    List<RoleEntity> findByName(String name);
}
