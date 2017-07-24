package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.dto.PlaceDTO;
import app.entity.PlaceEntity;

@Service
public interface PlaceService {
    long PLACE_ELEMENT_OTHER = 1;

    List<PlaceEntity> findAll();
    List<PlaceEntity> findAllRoot();
    PlaceEntity findById(Long id);

    PlaceEntity save(PlaceEntity entity);
    PlaceEntity save(PlaceDTO place);
    PlaceEntity delete(Long id);

    boolean hasChildren(Long id);

    List<PlaceDTO> entityToDTO(List<PlaceEntity> entityList);
    PlaceDTO entityToDTO(PlaceEntity parentEntity);
}
