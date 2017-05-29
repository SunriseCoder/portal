package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.UserConfirmEntity;

public interface UserConfirmRepository extends JpaRepository<UserConfirmEntity, Long> {
    UserConfirmEntity findByUserId(Long id);
}
