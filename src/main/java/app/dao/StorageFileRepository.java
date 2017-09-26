package app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.StorageFileEntity;
import app.entity.UserEntity;

public interface StorageFileRepository extends JpaRepository<StorageFileEntity, Long> {
    List<StorageFileEntity> findByCompletedIsFalseAndDeletedIsFalseAndUploadedBy(UserEntity user);
    List<StorageFileEntity> findByCompletedIsTrueAndPublishedIsFalseAndDeletedIsFalseAndUploadedBy(UserEntity user);
    List<StorageFileEntity> findByCompletedIsTrueAndPublishedIsTrueAndDeletedIsFalseAndUploadedBy(UserEntity user);
}
