package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByName(String name);
}
