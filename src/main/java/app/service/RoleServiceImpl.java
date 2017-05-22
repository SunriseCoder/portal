package app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.RoleRepository;
import app.entity.RoleEntity;

@Component
public class RoleServiceImpl implements RolesService {
    @Autowired
    RoleRepository repository;

    @Override
    public List<RoleEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public List<RoleEntity> getRoleByName(String name) {
        List<RoleEntity> roles = repository.findByName(name);
        return roles;
    }
}
