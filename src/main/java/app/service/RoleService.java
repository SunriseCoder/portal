package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.entity.RoleEntity;

@Service
public interface RoleService {
    List<RoleEntity> findAll();
    RoleEntity findById(Long id);
    RoleEntity findByName(String name);
    void save(RoleEntity roleEntity);
    void delete(RoleEntity roleEntity);
}
