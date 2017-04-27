package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByLogin(String login);
    UserEntity findByDisplayName(String displayName);
    UserEntity findByEmail(String email);
}
