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
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;

@Component
public class PlaceServiceImpl implements PlaceService {
    private static final Logger logger = LogManager.getLogger(PlaceServiceImpl.class.getName());

    @Autowired
    private AuditService auditService;

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
    public PlaceEntity save(PlaceDTO placeDto) {
        PlaceEntity placeEntity = new PlaceEntity(placeDto);
        String objectBefore = null;
        if (placeDto.getId() != null) {
            PlaceEntity storedPlace = findById(placeDto.getId());
            if (storedPlace != null) {
                placeEntity = storedPlace;
                placeEntity.setName(placeDto.getName());
                objectBefore = placeEntity.toString();
            }
        }

        if (placeDto.getParent() == null || placeDto.getParent().getId() == 0) {
            placeEntity.setParent(null);
        } else {
            PlaceEntity parent = findById(placeDto.getParent().getId());
            placeEntity.setParent(parent == null ? null : parent);
        }

        try {
            placeEntity = save(placeEntity);
            auditService.log(OperationTypes.CHANGE_PLACE_EDIT, AuditEventTypes.SUCCESSFUL, objectBefore, placeEntity.toString());
        } catch (Exception e) {
            logger.error("System error due to save place", e);
            auditService.log(OperationTypes.CHANGE_PLACE_EDIT, AuditEventTypes.SAVING_ERROR, objectBefore, placeEntity.toString(), e.getMessage());
            throw e;
        }
        return placeEntity;
    }

    @Override
    @Transactional
    public PlaceEntity delete(Long id) {
        PlaceEntity entity = repository.findOne(id);
        if (entity != null) {
            repository.delete(entity);
        }
        return entity;
    }

    @Override
    public boolean hasChildren(Long id) {
        long childrenAmount = repository.countByParentId(id);
        return childrenAmount > 0;
    }

    @Override
    public List<PlaceDTO> entityToDTO(List<PlaceEntity> entityList) {
        if (entityList == null) {
            return null;
        }

        List<PlaceDTO> dtoList = entityList.stream()
                        .map(entity -> entityToDTO(entity))
                        .collect(Collectors.toList());
        return dtoList;
    }

    @Override
    public PlaceDTO entityToDTO(PlaceEntity entity) {
        PlaceDTO dto = new PlaceDTO(entity);
        dto.setPath(entity.getPath());
        return dto;
    }
}
