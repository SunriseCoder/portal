package app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findBySystemIsFalse();
    UserEntity findByLogin(String login);
    UserEntity findByDisplayName(String displayName);
    UserEntity findByEmail(String email);
}
