package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.dto.FestivalDTO;
import app.entity.FestivalEntity;

@Service
public interface FestivalService {
    List<FestivalEntity> findAll();
    List<FestivalDTO> entityToDTO(List<FestivalEntity> entityList);
    FestivalEntity save(FestivalEntity entity);
}
