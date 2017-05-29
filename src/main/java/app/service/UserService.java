package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.entity.UserEntity;
import app.enums.Permissions;

@Service
public interface UserService {
    List<UserEntity> findAll();
    UserEntity findById(Long id);
    UserEntity findByLogin(String login);
    UserEntity findByDisplayName(String displayName);
    UserEntity findByEmail(String email);
    Boolean isAuthenticated();
    UserEntity getLoggedInUser();
    boolean hasPermission(Permissions permission);
    void save(UserEntity user);
    void encryptPass(UserEntity user);
    void confirmUser(Long id, String comment);
    void unconfirmUser(Long id);
    void lockUser(Long id, String reason);
    void unlockUser(Long id);
}
