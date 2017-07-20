package app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.entity.PlaceEntity;

@Service
public interface PlaceService {
    long PLACE_ELEMENT_OTHER = 1;

    List<PlaceEntity> findAll();
    PlaceEntity findById(Long id);
    PlaceEntity save(PlaceEntity entity);
}
