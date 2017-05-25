package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.PermissionEntity;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

}
