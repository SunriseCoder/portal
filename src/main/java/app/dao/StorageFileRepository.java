package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.StorageFileEntity;

public interface StorageFileRepository extends JpaRepository<StorageFileEntity, Long> {
}
