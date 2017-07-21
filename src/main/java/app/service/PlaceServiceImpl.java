package app.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.PlaceRepository;
import app.dto.PlaceDTO;
import app.entity.PlaceEntity;

@Component
public class PlaceServiceImpl implements PlaceService {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(PlaceServiceImpl.class.getName());

    @Autowired
    private PlaceRepository repository;

    @Override
    public List<PlaceEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public List<PlaceEntity> findAllRoot() {
        return repository.findAllByParentIsNull();
    }

    @Override
    public PlaceEntity findById(Long id) {
        return repository.findOne(id);
    }

    @Override
    @Transactional
    public PlaceEntity save(PlaceEntity entity) {
        return repository.save(entity);
    }

    @Override
    public List<PlaceDTO> entityToDTO(List<PlaceEntity> entityList) {
        if (entityList == null) {
            return null;
        }

        List<PlaceDTO> dtoList = entityList.stream()
                        .map(entity -> new PlaceDTO(entity))
                        .collect(Collectors.toList());
        return dtoList;
    }
}
