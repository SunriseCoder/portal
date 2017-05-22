package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.entity.RoleEntity;

@Service
public interface RolesService {
    List<RoleEntity> findAll();
    List<RoleEntity> getRoleByName(String name);
}
