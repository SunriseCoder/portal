package app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.PermissionRepository;
import app.entity.PermissionEntity;

@Component
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    PermissionRepository repository;

    @Override
    public List<PermissionEntity> findAll() {
        return repository.findAll();
    }
}
