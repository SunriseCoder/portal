package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.entity.PermissionEntity;

@Service
public interface PermissionService {
    List<PermissionEntity> findAll();
}
