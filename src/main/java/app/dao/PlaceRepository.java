package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import app.entity.PlaceEntity;

public interface PlaceRepository extends JpaRepository<PlaceEntity, Long>, JpaSpecificationExecutor<PlaceEntity> {

}
