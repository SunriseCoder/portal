package app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.FestivalRepository;
import app.dto.FestivalDTO;
import app.entity.FestivalEntity;

@Component
public class FestivalServiceImpl implements FestivalService {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(FestivalServiceImpl.class.getName());

    @Autowired
    private FestivalRepository repository;

    @Override
    public List<FestivalEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public List<FestivalDTO> entityToDTO(List<FestivalEntity> entityList) {
        List<FestivalDTO> dtoList = entityList.stream()
                        .map(entity -> new FestivalDTO(entity))
                        .collect(Collectors.toList());
        return dtoList;
    }
}
