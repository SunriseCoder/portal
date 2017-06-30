package app.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.RoleRepository;
import app.entity.RoleEntity;

@Component
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository repository;

    @Override
    public List<RoleEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public RoleEntity findById(Long id) {
        return repository.findOne(id);
    }

    @Override
    public RoleEntity findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    @Transactional
    public RoleEntity save(RoleEntity roleEntity) {
        return repository.save(roleEntity);
    }

    @Override
    @Transactional
    public void delete(RoleEntity roleEntity) {
        repository.delete(roleEntity);
    }
}
