package app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import app.entity.PlaceEntity;

public interface PlaceRepository extends JpaRepository<PlaceEntity, Long>, JpaSpecificationExecutor<PlaceEntity> {
    List<PlaceEntity> findAllByParentIsNull();
    long countByParentId(Long id);
}
