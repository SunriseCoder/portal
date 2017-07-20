package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.dto.FestivalDTO;
import app.entity.FestivalEntity;

@Service
public interface FestivalService {
    List<FestivalEntity> findAll();
    FestivalEntity findById(Long id);

    FestivalEntity save(FestivalEntity entity);
    FestivalEntity delete(Long id);

    List<FestivalDTO> entityToDTO(List<FestivalEntity> entityList);
}
