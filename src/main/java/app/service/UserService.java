package app.service;

import org.springframework.stereotype.Service;

import app.entity.UserEntity;

@Service
public interface UserService {
    UserEntity findByLogin(String login);
    UserEntity findByEmail(String email);
    Boolean isAuthenticated();
    UserEntity getLoggedInUser();
    void save(UserEntity user);
}
