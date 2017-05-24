package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.UserLockEntity;

public interface UserLockRepository extends JpaRepository<UserLockEntity, Long> {

}
