package app.service;

import org.springframework.stereotype.Service;

import app.entity.UserEntity;

@Service
public interface UserService {
    UserEntity findByLogin(String login);
}
