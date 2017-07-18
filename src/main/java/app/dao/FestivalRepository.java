package app.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import app.entity.FestivalEntity;

public interface FestivalRepository extends JpaRepository<FestivalEntity, Long>, JpaSpecificationExecutor<FestivalEntity> {

}
