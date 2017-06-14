package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.OperationTypeEntity;

public interface OperationTypeRepository extends JpaRepository<OperationTypeEntity, Long> {
    OperationTypeEntity findByName(String name);
}
