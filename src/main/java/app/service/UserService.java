package app.service;

import org.springframework.stereotype.Service;

import app.entity.UserEntity;

@Service
public interface UserService {
    UserEntity findByName(String name);
}
