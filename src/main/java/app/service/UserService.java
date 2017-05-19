package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.entity.UserEntity;

@Service
public interface UserService {
    List<UserEntity> findAll();
    UserEntity findById(Long id);
    UserEntity findByLogin(String login);
    UserEntity findByDisplayName(String displayName);
    UserEntity findByEmail(String email);
    Boolean isAuthenticated();
    UserEntity getLoggedInUser();
    void save(UserEntity user);
}
